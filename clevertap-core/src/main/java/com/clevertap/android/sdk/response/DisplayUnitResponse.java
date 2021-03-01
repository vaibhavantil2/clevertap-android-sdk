package com.clevertap.android.sdk.response;

import android.content.Context;
import com.clevertap.android.sdk.BaseCallbackManager;
import com.clevertap.android.sdk.CleverTapInstanceConfig;
import com.clevertap.android.sdk.Constants;
import com.clevertap.android.sdk.ControllerManager;
import com.clevertap.android.sdk.Logger;
import com.clevertap.android.sdk.displayunits.CTDisplayUnitController;
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

public class DisplayUnitResponse extends CleverTapResponseDecorator {

    private final Object displayUnitControllerLock = new Object();

    private CTDisplayUnitController mCTDisplayUnitController;

    private final BaseCallbackManager mCallbackManager;

    private final CleverTapResponse mCleverTapResponse;

    private final CleverTapInstanceConfig mConfig;

    private final Logger mLogger;

    private final ControllerManager mControllerManager;

    public DisplayUnitResponse(CleverTapResponse cleverTapResponse,
            CleverTapInstanceConfig config,
            BaseCallbackManager callbackManager, ControllerManager controllerManager) {
        mCleverTapResponse = cleverTapResponse;
        mConfig = config;
        mLogger = mConfig.getLogger();
        mCallbackManager = callbackManager;
        mControllerManager = controllerManager;
    }

    //Logic for the processing of Display Unit response

    @Override
    public void processResponse(final JSONObject response, final String stringBody, final Context context) {

        mLogger.verbose(mConfig.getAccountId(), "Processing Display Unit items...");

        if (mConfig.isAnalyticsOnly()) {
            mLogger.verbose(mConfig.getAccountId(),
                    "CleverTap instance is configured to analytics only, not processing Display Unit response");
            // process feature flag response
            mCleverTapResponse.processResponse(response, stringBody, context);
            return;
        }

        if (response == null) {
            mLogger.verbose(mConfig.getAccountId(), Constants.FEATURE_DISPLAY_UNIT
                    + "Can't parse Display Unit Response, JSON response object is null");
            return;
        }

        if (!response.has(Constants.DISPLAY_UNIT_JSON_RESPONSE_KEY)) {
            mLogger.verbose(mConfig.getAccountId(),
                    Constants.FEATURE_DISPLAY_UNIT + "JSON object doesn't contain the Display Units key");
            // process feature flag response
            mCleverTapResponse.processResponse(response, stringBody, context);
            return;
        }
        try {
            mLogger
                    .verbose(mConfig.getAccountId(),
                            Constants.FEATURE_DISPLAY_UNIT + "Processing Display Unit response");
            parseDisplayUnits(response.getJSONArray(Constants.DISPLAY_UNIT_JSON_RESPONSE_KEY));
        } catch (Throwable t) {
            mLogger.verbose(mConfig.getAccountId(), Constants.FEATURE_DISPLAY_UNIT + "Failed to parse response", t);
        }

        // process feature flag response
        mCleverTapResponse.processResponse(response, stringBody, context);
    }

    /**
     * Parses the Display Units using the JSON response
     *
     * @param messages - Json array of Display Unit items
     */
    private void parseDisplayUnits(JSONArray messages) {
        if (messages == null || messages.length() == 0) {
            mLogger.verbose(mConfig.getAccountId(),
                    Constants.FEATURE_DISPLAY_UNIT + "Can't parse Display Units, jsonArray is either empty or null");
            return;
        }

        synchronized (displayUnitControllerLock) {// lock to avoid multiple instance creation for controller
            if (mCTDisplayUnitController == null) {
                mCTDisplayUnitController = new CTDisplayUnitController();
                //TODO add a mechanism to transfer display unit lazily without using corestate instance
                //getCoreState().setCTDisplayUnitController(mCTDisplayUnitController);//TODO
                mControllerManager.setCTDisplayUnitController(mCTDisplayUnitController);
            }
        }
        ArrayList<CleverTapDisplayUnit> displayUnits = mControllerManager.getCTDisplayUnitController().updateDisplayUnits(messages);

        mCallbackManager.notifyDisplayUnitsLoaded(displayUnits);
    }
}