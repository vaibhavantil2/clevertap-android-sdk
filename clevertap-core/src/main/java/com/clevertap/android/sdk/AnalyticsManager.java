package com.clevertap.android.sdk;

import static com.clevertap.android.sdk.CTJsonConverter.getErrorObject;
import static com.clevertap.android.sdk.CTJsonConverter.getWzrkFields;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import com.clevertap.android.sdk.displayunits.CTDisplayUnitController;
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit;
import com.clevertap.android.sdk.inapp.CTInAppNotification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AnalyticsManager {

    private final HashMap<String, Integer> installReferrerMap = new HashMap<>(8);

    private final BaseQueueManager mBaseQueueManager;

    private final CTDisplayUnitController mCTDisplayUnitController;

    private final CallbackManager mCallbackManager;

    private final CleverTapInstanceConfig mConfig;

    private final Context mContext;

    private final CoreMetaData mCoreMetaData;

    private final DeviceInfo mDeviceInfo;

    private final LocalDataStore mLocalDataStore;

    private final MainLooperHandler mMainLooperHandler;

    private final NetworkManager mNetworkManager;

    private final PostAsyncSafelyHandler mPostAsyncSafelyHandler;

    private final ValidationResultStack mValidationResultStack;

    private final Validator mValidator;

    private final HashMap<String, Object> notificationIdTagMap = new HashMap<>();

    private final Object notificationMapLock = new Object();

    private final HashMap<String, Object> notificationViewedIdTagMap = new HashMap<>();

    AnalyticsManager(CoreState coreState) {
        mBaseQueueManager = coreState.getBaseEventQueueManager();

        mValidator = coreState.getValidator();
        mValidationResultStack = coreState.getValidationResultStack();
        mConfig = coreState.getConfig();
        mContext = coreState.getContext();
        mCoreMetaData = coreState.getCoreMetaData();
        mPostAsyncSafelyHandler = coreState.getPostAsyncSafelyHandler();
        mLocalDataStore = coreState.getLocalDataStore();
        mDeviceInfo = coreState.getDeviceInfo();
        mNetworkManager = (NetworkManager) coreState.getNetworkManager();
        mMainLooperHandler = coreState.getMainLooperHandler();
        mCallbackManager = coreState.getCallbackManager();
        mCTDisplayUnitController = coreState.getCTDisplayUnitController();
    }

    public void addMultiValuesForKey(final String key, final ArrayList<String> values) {
        mPostAsyncSafelyHandler.postAsyncSafely("addMultiValuesForKey", new Runnable() {
            @Override
            public void run() {
                final String command = (mLocalDataStore.getProfileValueForKey(key) != null)
                        ? Constants.COMMAND_ADD : Constants.COMMAND_SET;
                _handleMultiValues(values, key, command);
            }
        });
    }

    /**
     * This method is internal to the CleverTap SDK.
     * Developers should not use this method manually
     */
    public void fetchFeatureFlags() {
        if (mConfig.isAnalyticsOnly()) {
            return;
        }
        JSONObject event = new JSONObject();
        JSONObject notif = new JSONObject();
        try {
            notif.put("t", Constants.FETCH_TYPE_FF);
            event.put("evtName", Constants.WZRK_FETCH);
            event.put("evtData", notif);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mBaseQueueManager.queueEvent(mContext, event, Constants.FETCH_EVENT);
    }

    //Event
    public void forcePushAppLaunchedEvent() {
        mCoreMetaData.setAppLaunchPushed(false);
        pushAppLaunchedEvent();
    }

    public void pushAppLaunchedEvent() {
        if (mConfig.isDisableAppLaunchedEvent()) {
            mCoreMetaData.setAppLaunchPushed(true);
            mConfig.getLogger()
                    .debug(mConfig.getAccountId(), "App Launched Events disabled in the Android Manifest file");
            return;
        }
        if (mCoreMetaData.isAppLaunchPushed()) {
            mConfig.getLogger()
                    .verbose(mConfig.getAccountId(), "App Launched has already been triggered. Will not trigger it ");
            return;
        } else {
            mConfig.getLogger().verbose(mConfig.getAccountId(), "Firing App Launched event");
        }
        mCoreMetaData.setAppLaunchPushed(true);
        JSONObject event = new JSONObject();
        try {
            event.put("evtName", Constants.APP_LAUNCHED_EVENT);

            event.put("evtData", mNetworkManager
                    .getAppLaunchedFields());//TODO move this outside n/w mgr
        } catch (Throwable t) {
            // We won't get here
        }
        mBaseQueueManager.queueEvent(mContext, event, Constants.RAISED_EVENT);
    }

    public void pushDisplayUnitClickedEventForID(String unitID) {
        JSONObject event = new JSONObject();

        try {
            event.put("evtName", Constants.NOTIFICATION_CLICKED_EVENT_NAME);

            //wzrk fields
            if (mCTDisplayUnitController != null) {
                CleverTapDisplayUnit displayUnit = mCTDisplayUnitController
                        .getDisplayUnitForID(unitID);
                if (displayUnit != null) {
                    JSONObject eventExtraData = displayUnit.getWZRKFields();
                    if (eventExtraData != null) {
                        event.put("evtData", eventExtraData);
                        try {
                            mCoreMetaData.setWzrkParams(eventExtraData);
                        } catch (Throwable t) {
                            // no-op
                        }
                    }
                }
            }

            mBaseQueueManager.queueEvent(mContext, event, Constants.RAISED_EVENT);
        } catch (Throwable t) {
            // We won't get here
            mConfig.getLogger().verbose(mConfig.getAccountId(),
                    Constants.FEATURE_DISPLAY_UNIT + "Failed to push Display Unit clicked event" + t);
        }
    }

    @SuppressWarnings({"unused"})
    public void pushError(final String errorMessage, final int errorCode) {
        final HashMap<String, Object> props = new HashMap<>();
        props.put("Error Message", errorMessage);
        props.put("Error Code", errorCode);

        try {
            final String activityName = CoreMetaData.getCurrentActivityName();
            if (activityName != null) {
                props.put("Location", activityName);
            } else {
                props.put("Location", "Unknown");
            }
        } catch (Throwable t) {
            // Ignore
            props.put("Location", "Unknown");
        }

        pushEvent("Error Occurred", props);
    }

    public void pushEvent(String eventName, Map<String, Object> eventActions) {

        if (eventName == null || eventName.equals("")) {
            return;
        }

        ValidationResult validationResult = mValidator.isRestrictedEventName(eventName);
        // Check for a restricted event name
        if (validationResult.getErrorCode() > 0) {
            mValidationResultStack.pushValidationResult(validationResult);
            return;
        }

        ValidationResult discardedResult = mValidator.isEventDiscarded(eventName);
        // Check for a discarded event name
        if (discardedResult.getErrorCode() > 0) {
            mValidationResultStack.pushValidationResult(discardedResult);
            return;
        }

        if (eventActions == null) {
            eventActions = new HashMap<>();
        }

        JSONObject event = new JSONObject();
        try {
            // Validate
            ValidationResult vr = mValidator.cleanEventName(eventName);

            // Check for an error
            if (vr.getErrorCode() != 0) {
                event.put(Constants.ERROR_KEY, getErrorObject(vr));
            }

            eventName = vr.getObject().toString();
            JSONObject actions = new JSONObject();
            for (String key : eventActions.keySet()) {
                Object value = eventActions.get(key);
                vr = mValidator.cleanObjectKey(key);
                key = vr.getObject().toString();
                // Check for an error
                if (vr.getErrorCode() != 0) {
                    event.put(Constants.ERROR_KEY, getErrorObject(vr));
                }
                try {
                    vr = mValidator.cleanObjectValue(value, Validator.ValidationContext.Event);
                } catch (IllegalArgumentException e) {
                    // The object was neither a String, Boolean, or any number primitives
                    ValidationResult error = ValidationResultFactory
                            .create(512, Constants.PROP_VALUE_NOT_PRIMITIVE, eventName, key,
                                    value != null ? value.toString() : "");
                    mConfig.getLogger().debug(mConfig.getAccountId(), error.getErrorDesc());
                    mValidationResultStack.pushValidationResult(error);
                    // Skip this record
                    continue;
                }
                value = vr.getObject();
                // Check for an error
                if (vr.getErrorCode() != 0) {
                    event.put(Constants.ERROR_KEY, getErrorObject(vr));
                }
                actions.put(key, value);
            }
            event.put("evtName", eventName);
            event.put("evtData", actions);
            mBaseQueueManager.queueEvent(mContext, event, Constants.RAISED_EVENT);
        } catch (Throwable t) {
            // We won't get here
        }
    }

    /**
     * Raises the Notification Clicked event, if {@param clicked} is true,
     * otherwise the Notification Viewed event, if {@param clicked} is false.
     *
     * @param clicked    Whether or not this notification was clicked
     * @param data       The data to be attached as the event data
     * @param customData Additional data such as form input to to be added to the event data
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public void pushInAppNotificationStateEvent(boolean clicked, CTInAppNotification data, Bundle customData) {
        JSONObject event = new JSONObject();
        try {
            JSONObject notif = getWzrkFields(data);

            if (customData != null) {
                for (String x : customData.keySet()) {

                    Object value = customData.get(x);
                    if (value != null) {
                        notif.put(x, value);
                    }
                }
            }

            if (clicked) {
                try {
                    mCoreMetaData.setWzrkParams(notif);
                } catch (Throwable t) {
                    // no-op
                }
                event.put("evtName", Constants.NOTIFICATION_CLICKED_EVENT_NAME);
            } else {
                event.put("evtName", Constants.NOTIFICATION_VIEWED_EVENT_NAME);
            }

            event.put("evtData", notif);
            mBaseQueueManager.queueEvent(mContext, event, Constants.RAISED_EVENT);
        } catch (Throwable ignored) {
            // We won't get here
        }
    }

    public void pushInstallReferrer(String url) {
        try {
            mConfig.getLogger().verbose(mConfig.getAccountId(), "Referrer received: " + url);

            if (url == null) {
                return;
            }
            int now = (int) (System.currentTimeMillis() / 1000);

            //noinspection Constant Conditions
            if (installReferrerMap.containsKey(url) && now - installReferrerMap.get(url) < 10) {
                mConfig.getLogger()
                        .verbose(mConfig.getAccountId(),
                                "Skipping install referrer due to duplicate within 10 seconds");
                return;
            }

            installReferrerMap.put(url, now);

            Uri uri = Uri.parse("wzrk://track?install=true&" + url);

            pushDeepLink(uri, true);
        } catch (Throwable t) {
            // no-op
        }
    }

    public void pushNotificationClickedEvent(final Bundle extras) {

        if (mConfig.isAnalyticsOnly()) {
            mConfig.getLogger()
                    .debug(mConfig.getAccountId(),
                            "is Analytics Only - will not process Notification Clicked event.");
            return;
        }

        if (extras == null || extras.isEmpty() || extras.get(Constants.NOTIFICATION_TAG) == null) {
            mConfig.getLogger().debug(mConfig.getAccountId(),
                    "Push notification: " + (extras == null ? "NULL" : extras.toString())
                            + " not from CleverTap - will not process Notification Clicked event.");
            return;
        }

        String accountId = null;
        try {
            accountId = extras.getString(Constants.WZRK_ACCT_ID_KEY);
        } catch (Throwable t) {
            // no-op
        }

        boolean shouldProcess = (accountId == null && mConfig.isDefaultInstance())
                || mConfig.getAccountId()
                .equals(accountId);

        if (!shouldProcess) {
            mConfig.getLogger().debug(mConfig.getAccountId(),
                    "Push notification not targeted at this instance, not processing Notification Clicked Event");
            return;
        }

        if (extras.containsKey(Constants.INAPP_PREVIEW_PUSH_PAYLOAD_KEY)) {
            Runnable pendingInappRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        Logger.v("Received in-app via push payload: " + extras
                                .getString(Constants.INAPP_PREVIEW_PUSH_PAYLOAD_KEY));
                        JSONObject r = new JSONObject();
                        JSONArray inappNotifs = new JSONArray();
                        r.put(Constants.INAPP_JSON_RESPONSE_KEY, inappNotifs);
                        inappNotifs.put(new JSONObject(extras.getString(Constants.INAPP_PREVIEW_PUSH_PAYLOAD_KEY)));

                        processInAppResponse(r, mContext);
                    } catch (Throwable t) {
                        Logger.v("Failed to display inapp notification from push notification payload", t);
                    }
                }
            };

            mMainLooperHandler.setPendingRunnable(pendingInappRunnable);
            return;
        }

        if (extras.containsKey(Constants.INBOX_PREVIEW_PUSH_PAYLOAD_KEY)) {
            Runnable pendingInboxRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        Logger.v("Received inbox via push payload: " + extras
                                .getString(Constants.INBOX_PREVIEW_PUSH_PAYLOAD_KEY));
                        JSONObject r = new JSONObject();
                        JSONArray inappNotifs = new JSONArray();
                        r.put(Constants.INBOX_JSON_RESPONSE_KEY, inappNotifs);
                        JSONObject testPushObject = new JSONObject(
                                extras.getString(Constants.INBOX_PREVIEW_PUSH_PAYLOAD_KEY));
                        testPushObject.put("_id", String.valueOf(System.currentTimeMillis() / 1000));
                        inappNotifs.put(testPushObject);
                        processInboxResponse(r);
                    } catch (Throwable t) {
                        Logger.v("Failed to process inbox message from push notification payload", t);
                    }
                }
            };
            mMainLooperHandler.setPendingRunnable(pendingInboxRunnable);
            return;
        }

        if (extras.containsKey(Constants.DISPLAY_UNIT_PREVIEW_PUSH_PAYLOAD_KEY)) {
            handleSendTestForDisplayUnits(extras);
            return;
        }

        if (!extras.containsKey(Constants.NOTIFICATION_ID_TAG) || (extras.getString(Constants.NOTIFICATION_ID_TAG)
                == null)) {
            mConfig.getLogger().debug(mConfig.getAccountId(),
                    "Push notification ID Tag is null, not processing Notification Clicked event for:  " + extras
                            .toString());
            return;
        }

        // Check for dupe notification views; if same notficationdId within specified time interval (5 secs) don't process
        boolean isDuplicate = checkDuplicateNotificationIds(extras, notificationIdTagMap,
                Constants.NOTIFICATION_ID_TAG_INTERVAL);
        if (isDuplicate) {
            mConfig.getLogger().debug(mConfig.getAccountId(),
                    "Already processed Notification Clicked event for " + extras.toString()
                            + ", dropping duplicate.");
            return;
        }

        JSONObject event = new JSONObject();
        JSONObject notif = new JSONObject();
        try {
            for (String x : extras.keySet()) {
                if (!x.startsWith(Constants.WZRK_PREFIX)) {
                    continue;
                }
                Object value = extras.get(x);
                notif.put(x, value);
            }

            event.put("evtName", Constants.NOTIFICATION_CLICKED_EVENT_NAME);
            event.put("evtData", notif);
            mBaseQueueManager.queueEvent(mContext, event, Constants.RAISED_EVENT);

            try {
                mCoreMetaData.setWzrkParams(getWzrkFields(extras));
            } catch (Throwable t) {
                // no-op
            }
        } catch (Throwable t) {
            // We won't get here
        }
        if (mCallbackManager.getPushNotificationListener() != null) {
            mCallbackManager.getPushNotificationListener()
                    .onNotificationClickedPayloadReceived(Utils.convertBundleObjectToHashMap(extras));
        } else {
            Logger.d("CTPushNotificationListener is not set");
        }
    }

    /**
     * Pushes the Notification Viewed event to CleverTap.
     *
     * @param extras The {@link Bundle} object that contains the
     *               notification details
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public void pushNotificationViewedEvent(Bundle extras) {

        if (extras == null || extras.isEmpty() || extras.get(Constants.NOTIFICATION_TAG) == null) {
            mConfig.getLogger().debug(mConfig.getAccountId(),
                    "Push notification: " + (extras == null ? "NULL" : extras.toString())
                            + " not from CleverTap - will not process Notification Viewed event.");
            return;
        }

        if (!extras.containsKey(Constants.NOTIFICATION_ID_TAG) || (extras.getString(Constants.NOTIFICATION_ID_TAG)
                == null)) {
            mConfig.getLogger().debug(mConfig.getAccountId(),
                    "Push notification ID Tag is null, not processing Notification Viewed event for:  " + extras
                            .toString());
            return;
        }

        // Check for dupe notification views; if same notficationdId within specified time interval (2 secs) don't process
        boolean isDuplicate = checkDuplicateNotificationIds(extras, notificationViewedIdTagMap,
                Constants.NOTIFICATION_VIEWED_ID_TAG_INTERVAL);
        if (isDuplicate) {
            mConfig.getLogger().debug(mConfig.getAccountId(),
                    "Already processed Notification Viewed event for " + extras.toString() + ", dropping duplicate.");
            return;
        }

        mConfig.getLogger().debug("Recording Notification Viewed event for notification:  " + extras.toString());

        JSONObject event = new JSONObject();
        try {
            JSONObject notif = CTJsonConverter.getWzrkFields(extras);
            event.put("evtName", Constants.NOTIFICATION_VIEWED_EVENT_NAME);
            event.put("evtData", notif);
        } catch (Throwable ignored) {
            //no-op
        }
        mBaseQueueManager.queueEvent(mContext, event, Constants.NV_EVENT);
    }

    public void pushProfile(final Map<String, Object> profile) {
        if (profile == null || profile.isEmpty()) {
            return;
        }

        mPostAsyncSafelyHandler.postAsyncSafely("profilePush", new Runnable() {
            @Override
            public void run() {
                _push(profile);
            }
        });
    }

    public void removeMultiValuesForKey(final String key, final ArrayList<String> values) {
        mPostAsyncSafelyHandler.postAsyncSafely("removeMultiValuesForKey", new Runnable() {
            @Override
            public void run() {
                _handleMultiValues(values, key, Constants.COMMAND_REMOVE);
            }
        });
    }

    public void removeValueForKey(final String key) {
        mPostAsyncSafelyHandler.postAsyncSafely("removeValueForKey", new Runnable() {
            @Override
            public void run() {
                _removeValueForKey(key);
            }
        });
    }

    void _generateEmptyMultiValueError(String key) {
        ValidationResult error = ValidationResultFactory.create(512, Constants.INVALID_MULTI_VALUE, key);
        mValidationResultStack.pushValidationResult(error);
        mConfig.getLogger().debug(mConfig.getAccountId(), error.getErrorDesc());
    }

    void pushChargedEvent(HashMap<String, Object> chargeDetails,
            ArrayList<HashMap<String, Object>> items) {

        if (chargeDetails == null || items == null) {
            mConfig.getLogger().debug(mConfig.getAccountId(), "Invalid Charged event: details and or items is null");
            return;
        }

        if (items.size() > 50) {
            ValidationResult error = ValidationResultFactory.create(522);
            mConfig.getLogger().debug(mConfig.getAccountId(), error.getErrorDesc());
            mValidationResultStack.pushValidationResult(error);
        }

        JSONObject evtData = new JSONObject();
        JSONObject chargedEvent = new JSONObject();
        ValidationResult vr;
        try {
            for (String key : chargeDetails.keySet()) {
                Object value = chargeDetails.get(key);
                vr = mValidator.cleanObjectKey(key);
                key = vr.getObject().toString();
                // Check for an error
                if (vr.getErrorCode() != 0) {
                    chargedEvent.put(Constants.ERROR_KEY, getErrorObject(vr));
                }

                try {
                    vr = mValidator.cleanObjectValue(value, Validator.ValidationContext.Event);
                } catch (IllegalArgumentException e) {
                    // The object was neither a String, Boolean, or any number primitives
                    ValidationResult error = ValidationResultFactory.create(511,
                            Constants.PROP_VALUE_NOT_PRIMITIVE, "Charged", key,
                            value != null ? value.toString() : "");
                    mValidationResultStack.pushValidationResult(error);
                    mConfig.getLogger().debug(mConfig.getAccountId(), error.getErrorDesc());
                    // Skip this property
                    continue;
                }
                value = vr.getObject();
                // Check for an error
                if (vr.getErrorCode() != 0) {
                    chargedEvent.put(Constants.ERROR_KEY, getErrorObject(vr));
                }

                evtData.put(key, value);
            }

            JSONArray jsonItemsArray = new JSONArray();
            for (HashMap<String, Object> map : items) {
                JSONObject itemDetails = new JSONObject();
                for (String key : map.keySet()) {
                    Object value = map.get(key);
                    vr = mValidator.cleanObjectKey(key);
                    key = vr.getObject().toString();
                    // Check for an error
                    if (vr.getErrorCode() != 0) {
                        chargedEvent.put(Constants.ERROR_KEY, getErrorObject(vr));
                    }

                    try {
                        vr = mValidator.cleanObjectValue(value, Validator.ValidationContext.Event);
                    } catch (IllegalArgumentException e) {
                        // The object was neither a String, Boolean, or any number primitives
                        ValidationResult error = ValidationResultFactory
                                .create(511, Constants.OBJECT_VALUE_NOT_PRIMITIVE, key,
                                        value != null ? value.toString() : "");
                        mConfig.getLogger().debug(mConfig.getAccountId(), error.getErrorDesc());
                        mValidationResultStack.pushValidationResult(error);
                        // Skip this property
                        continue;
                    }
                    value = vr.getObject();
                    // Check for an error
                    if (vr.getErrorCode() != 0) {
                        chargedEvent.put(Constants.ERROR_KEY, getErrorObject(vr));
                    }
                    itemDetails.put(key, value);
                }
                jsonItemsArray.put(itemDetails);
            }
            evtData.put("Items", jsonItemsArray);

            chargedEvent.put("evtName", Constants.CHARGED_EVENT);
            chargedEvent.put("evtData", evtData);
            mBaseQueueManager.queueEvent(mContext, chargedEvent, Constants.RAISED_EVENT);
        } catch (Throwable t) {
            // We won't get here
        }
    }

    synchronized void pushDeepLink(Uri uri, boolean install) {
        if (uri == null) {
            return;
        }

        try {
            JSONObject referrer = UriHelper.getUrchinFromUri(uri);
            if (referrer.has("us")) {
                mCoreMetaData.setSource(referrer.get("us").toString());
            }
            if (referrer.has("um")) {
                mCoreMetaData.setMedium(referrer.get("um").toString());
            }
            if (referrer.has("uc")) {
                mCoreMetaData.setCampaign(referrer.get("uc").toString());
            }

            referrer.put("referrer", uri.toString());
            if (install) {
                referrer.put("install", true);
            }
            recordPageEventWithExtras(referrer);

        } catch (Throwable t) {
            mConfig.getLogger().verbose(mConfig.getAccountId(), "Failed to push deep link", t);
        }
    }

    Future<?> raiseEventForGeofences(String eventName, JSONObject geofenceProperties) {

        Future<?> future = null;

        JSONObject event = new JSONObject();
        try {
            event.put("evtName", eventName);
            event.put("evtData", geofenceProperties);

            Location location = new Location("");
            location.setLatitude(geofenceProperties.getDouble("triggered_lat"));
            location.setLongitude(geofenceProperties.getDouble("triggered_lng"));

            geofenceProperties.remove("triggered_lat");
            geofenceProperties.remove("triggered_lng");

            mCoreMetaData.setLocationFromUser(location);

            future = mBaseQueueManager.queueEvent(mContext, event, Constants.RAISED_EVENT);
        } catch (JSONException e) {
            mConfig.getLogger().debug(mConfig.getAccountId(), Constants.LOG_TAG_GEOFENCES +
                    "JSON Exception when raising GeoFence event "
                    + eventName + " - " + e.getLocalizedMessage());
        }

        return future;
    }

    void recordPageEventWithExtras(JSONObject extras) {
        try {
            JSONObject jsonObject = new JSONObject();
            // Add the extras
            if (extras != null && extras.length() > 0) {
                Iterator keys = extras.keys();
                while (keys.hasNext()) {
                    try {
                        String key = (String) keys.next();
                        jsonObject.put(key, extras.getString(key));
                    } catch (ClassCastException ignore) {
                        // Really won't get here
                    }
                }
            }
            mBaseQueueManager.queueEvent(mContext, jsonObject, Constants.PAGE_EVENT);
        } catch (Throwable t) {
            // We won't get here
        }
    }

    void setMultiValuesForKey(final String key, final ArrayList<String> values) {
        mPostAsyncSafelyHandler.postAsyncSafely("setMultiValuesForKey", new Runnable() {
            @Override
            public void run() {
                _handleMultiValues(values, key, Constants.COMMAND_SET);
            }
        });
    }

    private JSONArray _cleanMultiValues(ArrayList<String> values, String key) {

        try {
            if (values == null || key == null) {
                return null;
            }

            JSONArray cleanedValues = new JSONArray();
            ValidationResult vr;

            // loop through and clean the new values
            for (String value : values) {
                value = (value == null) ? "" : value;  // so we will generate a validation error later on

                // validate value
                vr = mValidator.cleanMultiValuePropertyValue(value);

                // Check for an error
                if (vr.getErrorCode() != 0) {
                    mValidationResultStack.pushValidationResult(vr);
                }

                // reset the value
                Object _value = vr.getObject();
                value = (_value != null) ? vr.getObject().toString() : null;

                // if value is empty generate an error and return
                if (value == null || value.isEmpty()) {
                    _generateEmptyMultiValueError(key);
                    // Abort
                    return null;
                }
                // add to the newValues to be merged
                cleanedValues.put(value);
            }

            return cleanedValues;

        } catch (Throwable t) {
            mConfig.getLogger().verbose(mConfig.getAccountId(), "Error cleaning multi values for key " + key, t);
            _generateEmptyMultiValueError(key);
            return null;
        }
    }

    private JSONArray _constructExistingMultiValue(String key, String command) {

        boolean remove = command.equals(Constants.COMMAND_REMOVE);
        boolean add = command.equals(Constants.COMMAND_ADD);

        // only relevant for add's and remove's; a set overrides the existing value, so return a new array
        if (!remove && !add) {
            return new JSONArray();
        }

        Object existing = _getProfilePropertyIgnorePersonalizationFlag(key);

        // if there is no existing value
        if (existing == null) {
            // if its a remove then return null to abort operation
            // no point in running remove against a nonexistent value
            if (remove) {
                return null;
            }

            // otherwise return an empty array
            return new JSONArray();
        }

        // value exists

        // the value should only ever be a JSONArray or scalar (String really)

        // if its already a JSONArray return that
        if (existing instanceof JSONArray) {
            return (JSONArray) existing;
        }

        // handle a scalar value as the existing value
        /*
            if its an add, our rule is to promote the scalar value to multi value and include the cleaned stringified
            scalar value as the first element of the resulting array

            NOTE: the existing scalar value is currently limited to 120 bytes; when adding it to a multi value
            it is subject to the current 40 byte limit

            if its a remove, our rule is to delete the key from the local copy
            if the cleaned stringified existing value is equal to any of the cleaned values passed to the remove method

            if its an add, return an empty array as the default,
            in the event the existing scalar value fails stringifying/cleaning

            returning null will signal that a remove operation should be aborted,
            as there is no valid promoted multi value to remove against
         */

        JSONArray _default = (add) ? new JSONArray() : null;

        String stringified = _stringifyAndCleanScalarProfilePropValue(existing);

        return (stringified != null) ? new JSONArray().put(stringified) : _default;
    }

    private void _generateInvalidMultiValueKeyError(String key) {
        ValidationResult error = ValidationResultFactory.create(523, Constants.INVALID_MULTI_VALUE_KEY, key);
        mValidationResultStack.pushValidationResult(error);
        mConfig.getLogger().debug(mConfig.getAccountId(),
                "Invalid multi-value property key " + key + " profile multi value operation aborted");
    }

    // use for internal profile getter doesn't do the personalization check
    private Object _getProfilePropertyIgnorePersonalizationFlag(String key) {
        return mLocalDataStore.getProfileValueForKey(key);
    }

    private void _handleMultiValues(ArrayList<String> values, String key, String command) {
        if (key == null) {
            return;
        }

        if (values == null || values.isEmpty()) {
            _generateEmptyMultiValueError(key);
            return;
        }

        ValidationResult vr;

        // validate the key
        vr = mValidator.cleanMultiValuePropertyKey(key);

        // Check for an error
        if (vr.getErrorCode() != 0) {
            mValidationResultStack.pushValidationResult(vr);
        }

        // reset the key
        Object _key = vr.getObject();
        String cleanKey = (_key != null) ? vr.getObject().toString() : null;

        // if key is empty generate an error and return
        if (cleanKey == null || cleanKey.isEmpty()) {
            _generateInvalidMultiValueKeyError(key);
            return;
        }

        key = cleanKey;

        try {
            JSONArray currentValues = _constructExistingMultiValue(key, command);
            JSONArray newValues = _cleanMultiValues(values, key);
            _validateAndPushMultiValue(currentValues, newValues, values, key, command);

        } catch (Throwable t) {
            mConfig.getLogger()
                    .verbose(mConfig.getAccountId(), "Error handling multi value operation for key " + key, t);
        }
    }

    private void _push(Map<String, Object> profile) {
        if (profile == null || profile.isEmpty()) {
            return;
        }

        try {
            ValidationResult vr;
            JSONObject customProfile = new JSONObject();
            JSONObject fieldsToUpdateLocally = new JSONObject();
            for (String key : profile.keySet()) {
                Object value = profile.get(key);

                vr = mValidator.cleanObjectKey(key);
                key = vr.getObject().toString();
                // Check for an error
                if (vr.getErrorCode() != 0) {
                    mValidationResultStack.pushValidationResult(vr);
                }

                if (key.isEmpty()) {
                    ValidationResult keyError = ValidationResultFactory.create(512, Constants.PUSH_KEY_EMPTY);
                    mValidationResultStack.pushValidationResult(keyError);
                    mConfig.getLogger().debug(mConfig.getAccountId(), keyError.getErrorDesc());
                    // Skip this property
                    continue;
                }

                try {
                    vr = mValidator.cleanObjectValue(value, Validator.ValidationContext.Profile);
                } catch (Throwable e) {
                    // The object was neither a String, Boolean, or any number primitives
                    ValidationResult error = ValidationResultFactory.create(512,
                            Constants.OBJECT_VALUE_NOT_PRIMITIVE_PROFILE,
                            value != null ? value.toString() : "", key);
                    mValidationResultStack.pushValidationResult(error);
                    mConfig.getLogger().debug(mConfig.getAccountId(), error.getErrorDesc());
                    // Skip this property
                    continue;
                }
                value = vr.getObject();
                // Check for an error
                if (vr.getErrorCode() != 0) {
                    mValidationResultStack.pushValidationResult(vr);
                }

                // test Phone:  if no device country code, test if phone starts with +, log but always send
                if (key.equalsIgnoreCase("Phone")) {
                    try {
                        value = value.toString();
                        String countryCode = mDeviceInfo.getCountryCode();
                        if (countryCode == null || countryCode.isEmpty()) {
                            String _value = (String) value;
                            if (!_value.startsWith("+")) {
                                ValidationResult error = ValidationResultFactory
                                        .create(512, Constants.INVALID_COUNTRY_CODE, _value);
                                mValidationResultStack.pushValidationResult(error);
                                mConfig.getLogger().debug(mConfig.getAccountId(), error.getErrorDesc());
                            }
                        }
                        mConfig.getLogger().verbose(mConfig.getAccountId(),
                                "Profile phone is: " + value + " device country code is: " + ((countryCode != null)
                                        ? countryCode : "null"));
                    } catch (Exception e) {
                        mValidationResultStack
                                .pushValidationResult(ValidationResultFactory.create(512, Constants.INVALID_PHONE));
                        mConfig.getLogger()
                                .debug(mConfig.getAccountId(), "Invalid phone number: " + e.getLocalizedMessage());
                        continue;
                    }
                }

                // add to the local profile update object
                fieldsToUpdateLocally.put(key, value);
                customProfile.put(key, value);
            }

            mConfig.getLogger()
                    .verbose(mConfig.getAccountId(), "Constructed custom profile: " + customProfile.toString());

            // update local profile values
            if (fieldsToUpdateLocally.length() > 0) {
                mLocalDataStore.setProfileFields(fieldsToUpdateLocally);
            }

            mBaseQueueManager.pushBasicProfile(customProfile);

        } catch (Throwable t) {
            // Will not happen
            mConfig.getLogger().verbose(mConfig.getAccountId(), "Failed to push profile", t);
        }
    }

    private void _removeValueForKey(String key) {
        try {
            key = (key == null) ? "" : key; // so we will generate a validation error later on

            // validate the key
            ValidationResult vr;

            vr = mValidator.cleanObjectKey(key);
            key = vr.getObject().toString();

            if (key.isEmpty()) {
                ValidationResult error = ValidationResultFactory.create(512, Constants.KEY_EMPTY);
                mValidationResultStack.pushValidationResult(error);
                mConfig.getLogger().debug(mConfig.getAccountId(), error.getErrorDesc());
                // Abort
                return;
            }
            // Check for an error
            if (vr.getErrorCode() != 0) {
                mValidationResultStack.pushValidationResult(vr);
            }

            // remove from the local profile
            mLocalDataStore.removeProfileField(key);

            // send the delete command
            JSONObject command = new JSONObject().put(Constants.COMMAND_DELETE, true);
            JSONObject update = new JSONObject().put(key, command);
            mBaseQueueManager.pushBasicProfile(update);

            mConfig.getLogger()
                    .verbose(mConfig.getAccountId(), "removing value for key " + key + " from user profile");

        } catch (Throwable t) {
            mConfig.getLogger().verbose(mConfig.getAccountId(), "Failed to remove profile value for key " + key, t);
        }
    }

    private String _stringifyAndCleanScalarProfilePropValue(Object value) {
        String val = CTJsonConverter.toJsonString(value);

        if (val != null) {
            ValidationResult vr = mValidator.cleanMultiValuePropertyValue(val);

            // Check for an error
            if (vr.getErrorCode() != 0) {
                mValidationResultStack.pushValidationResult(vr);
            }

            Object _value = vr.getObject();
            val = (_value != null) ? vr.getObject().toString() : null;
        }

        return val;
    }

    private void _validateAndPushMultiValue(JSONArray currentValues, JSONArray newValues,
            ArrayList<String> originalValues, String key, String command) {

        try {

            // if any of these are null, indicates some problem along the way so abort operation
            if (currentValues == null || newValues == null || originalValues == null || key == null
                    || command == null) {
                return;
            }

            String mergeOperation = command.equals(Constants.COMMAND_REMOVE) ? Validator.REMOVE_VALUES_OPERATION
                    : Validator.ADD_VALUES_OPERATION;

            // merge currentValues and newValues
            ValidationResult vr = mValidator
                    .mergeMultiValuePropertyForKey(currentValues, newValues, mergeOperation, key);

            // Check for an error
            if (vr.getErrorCode() != 0) {
                mValidationResultStack.pushValidationResult(vr);
            }

            // set the merged local values array
            JSONArray localValues = (JSONArray) vr.getObject();

            // update local profile
            // remove an empty array
            if (localValues == null || localValues.length() <= 0) {
                mLocalDataStore.removeProfileField(key);
            } else {
                // not empty so save to local profile
                mLocalDataStore.setProfileField(key, localValues);
            }

            // push to server
            JSONObject commandObj = new JSONObject();
            commandObj.put(command, new JSONArray(originalValues));

            JSONObject fields = new JSONObject();
            fields.put(key, commandObj);

            mBaseQueueManager.pushBasicProfile(fields);

            mConfig.getLogger()
                    .verbose(mConfig.getAccountId(), "Constructed multi-value profile push: " + fields.toString());

        } catch (Throwable t) {
            mConfig.getLogger().verbose(mConfig.getAccountId(), "Error pushing multiValue for key " + key, t);
        }
    }

    private boolean checkDuplicateNotificationIds(Bundle extras, HashMap<String, Object> notificationTagMap,
            int interval) {
        synchronized (notificationMapLock) {
            // default to false; only return true if we are sure we've seen this one before
            boolean isDupe = false;
            try {
                String notificationIdTag = extras.getString(Constants.NOTIFICATION_ID_TAG);
                long now = System.currentTimeMillis();
                if (notificationTagMap.containsKey(notificationIdTag)) {
                    long timestamp;
                    // noinspection ConstantConditions
                    timestamp = (Long) notificationTagMap.get(notificationIdTag);
                    // same notificationId within time internal treat as dupe
                    if (now - timestamp < interval) {
                        isDupe = true;
                    }
                }
                notificationTagMap.put(notificationIdTag, now);
            } catch (Throwable ignored) {
                // no-op
            }
            return isDupe;
        }
    }

    public void pushDisplayUnitViewedEventForID(String unitID) {
        JSONObject event = new JSONObject();

        try {
            event.put("evtName", Constants.NOTIFICATION_VIEWED_EVENT_NAME);

            //wzrk fields
            if (mCTDisplayUnitController != null) {
                CleverTapDisplayUnit displayUnit = mCTDisplayUnitController
                        .getDisplayUnitForID(unitID);
                if (displayUnit != null) {
                    JSONObject eventExtras = displayUnit.getWZRKFields();
                    if (eventExtras != null) {
                        event.put("evtData", eventExtras);
                    }
                }
            }

            mBaseQueueManager.queueEvent(mContext, event, Constants.RAISED_EVENT);
        } catch (Throwable t) {
            // We won't get here
            mConfig.getLogger().verbose(mConfig.getAccountId(),
                    Constants.FEATURE_DISPLAY_UNIT + "Failed to push Display Unit viewed event" + t);
        }
    }

    public synchronized void pushInstallReferrer(String source, String medium, String campaign) {
        if (source == null && medium == null && campaign == null) {
            return;
        }
        try {
            // If already pushed, don't send it again
            int status = StorageHelper.getInt(mContext, "app_install_status", 0);
            if (status != 0) {
                Logger.d("Install referrer has already been set. Will not override it");
                return;
            }
            StorageHelper.putInt(mContext, "app_install_status", 1);

            if (source != null) {
                source = Uri.encode(source);
            }
            if (medium != null) {
                medium = Uri.encode(medium);
            }
            if (campaign != null) {
                campaign = Uri.encode(campaign);
            }

            String uriStr = "wzrk://track?install=true";
            if (source != null) {
                uriStr += "&utm_source=" + source;
            }
            if (medium != null) {
                uriStr += "&utm_medium=" + medium;
            }
            if (campaign != null) {
                uriStr += "&utm_campaign=" + campaign;
            }

            Uri uri = Uri.parse(uriStr);
            pushDeepLink(uri, true);
        } catch (Throwable t) {
            Logger.v("Failed to push install referrer", t);
        }
    }
}