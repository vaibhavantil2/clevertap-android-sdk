package com.clevertap.android.sdk;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class Constants {
    static final String LABEL_ACCOUNT_ID = "CLEVERTAP_ACCOUNT_ID";
    static final String LABEL_TOKEN = "CLEVERTAP_TOKEN";
    static final String LABEL_NOTIFICATION_ICON = "CLEVERTAP_NOTIFICATION_ICON";
    static final String LABEL_INAPP_EXCLUDE = "CLEVERTAP_INAPP_EXCLUDE";
    static final String LABEL_REGION = "CLEVERTAP_REGION";
    static final String LABEL_DISABLE_APP_LAUNCH = "CLEVERTAP_DISABLE_APP_LAUNCHED";
    static final String LABEL_SSL_PINNING = "CLEVERTAP_SSL_PINNING";
    static final String LABEL_BACKGROUND_SYNC = "CLEVERTAP_BACKGROUND_SYNC";
    static final String LABEL_CUSTOM_ID = "CLEVERTAP_USE_CUSTOM_ID";
    static final String LABEL_USE_GOOGLE_AD_ID = "CLEVERTAP_USE_GOOGLE_AD_ID";
    static final String LABEL_FCM_SENDER_ID = "FCM_SENDER_ID";
    static final String LABEL_PACKAGE_NAME = "CLEVERTAP_APP_PACKAGE";
    static final String LABEL_BETA = "CLEVERTAP_BETA";
    static final String LABEL_LC = "CLEVERTAP_LC";
    static final String LABEL_INTENT_SERVICE = "CLEVERTAP_INTENT_SERVICE";
    static final String CLEVERTAP_OPTOUT = "ct_optout";
    static final String CLEVERTAP_STORAGE_TAG = "WizRocket";
    static final String CLEVERTAP_LOG_TAG = "CleverTap";
    static final int SESSION_LENGTH_MINS = 20;
    static final String DEVICE_ID_TAG = "deviceId";
    static final String FALLBACK_ID_TAG = "fallbackId";
    static final SimpleDateFormat FB_DOB_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
    static final SimpleDateFormat GP_DOB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    static final int PAGE_EVENT = 1;
    static final int PING_EVENT = 2;
    static final int PROFILE_EVENT = 3;
    static final int RAISED_EVENT = 4;
    static final int DATA_EVENT = 5;
    static final int NV_EVENT = 6;
    static final int FETCH_EVENT = 7;
    static final String WZRK_FETCH = "wzrk_fetch";
    static final String ICON_BASE_URL = "http://static.wizrocket.com/android/ico/";
    static final String NOTIFICATION_CLICKED_EVENT_NAME = "Notification Clicked";
    static final String NOTIFICATION_VIEWED_EVENT_NAME = "Notification Viewed";
    static final String GEOFENCE_ENTERED_EVENT_NAME = "Geocluster Entered";
    static final String GEOFENCE_EXITED_EVENT_NAME = "Geocluster Exited";
    static final String APP_LAUNCHED_EVENT = "App Launched";
    static final String ERROR_KEY = "wzrk_error";
    static final int PUSH_DELAY_MS = 1000;
    static final String INAPP_PREVIEW_PUSH_PAYLOAD_KEY = "wzrk_inapp";
    static final String INBOX_PREVIEW_PUSH_PAYLOAD_KEY = "wzrk_inbox";
    static final String DISPLAY_UNIT_PREVIEW_PUSH_PAYLOAD_KEY = "wzrk_adunit";
    static final String INAPP_HTML_TAG = "html";
    static final String INAPP_DATA_TAG = "d";
    static final String INAPP_X_PERCENT = "xp";
    static final String INAPP_Y_PERCENT = "yp";
    static final String INAPP_X_DP = "xdp";
    static final String INAPP_Y_DP = "ydp";
    static final String INAPP_POSITION = "pos";
    static final char INAPP_POSITION_TOP = 't';
    static final char INAPP_POSITION_RIGHT = 'r';
    static final char INAPP_POSITION_BOTTOM = 'b';
    static final char INAPP_POSITION_LEFT = 'l';
    static final char INAPP_POSITION_CENTER = 'c';
    static final String INAPP_NOTIF_DARKEN_SCREEN = "dk";
    static final String INAPP_NOTIF_SHOW_CLOSE = "sc";
    static final String INAPP_JSON_RESPONSE_KEY = "inapp_notifs";
    static final String INBOX_JSON_RESPONSE_KEY = "inbox_notifs";
    static final String DISPLAY_UNIT_JSON_RESPONSE_KEY = "adUnit_notifs";
    static final String FEATURE_FLAG_JSON_RESPONSE_KEY = "ff_notifs";
    static final String REMOTE_CONFIG_FLAG_JSON_RESPONSE_KEY = "pc_notifs";
    static final String GEOFENCES_JSON_RESPONSE_KEY = "geofences";
    static final String DISCARDED_EVENT_JSON_KEY = "d_e";
    static final String INAPP_MAX_DISPLAY_COUNT = "mdc";
    static final String INAPP_MAX_PER_SESSION = "imc";
    static final String INAPP_WINDOW = "w";
    static final String INAPP_KEY = "inApp";
    static final int INAPP_CLOSE_IV_WIDTH = 40;
    static final String INAPP_JS_ENABLED = "isJsEnabled";
    public static final String NOTIFICATION_ID_TAG = "wzrk_id";
    static final String DEEP_LINK_KEY = "wzrk_dl";
    public static final String WZRK_PREFIX = "wzrk_";
    static final int NOTIFICATION_ID_TAG_INTERVAL = 5000;
    static final int NOTIFICATION_VIEWED_ID_TAG_INTERVAL = 2000;
    static final String SESSION_ID_LAST = "lastSessionId";
    static final String LAST_SESSION_EPOCH = "sexe";
    static final int MAX_KEY_LENGTH = 120;
    static final int MAX_VALUE_LENGTH = 512;
    static final int MAX_MULTI_VALUE_ARRAY_LENGTH = 100;
    static final int MAX_MULTI_VALUE_LENGTH = 512;
    static final String WZRK_FROM_KEY = "wzrk_from";
    static final String WZRK_ACCT_ID_KEY = "wzrk_acct_id";
    static final String WZRK_FROM = "CTPushNotificationReceiver";
    static final String NETWORK_INFO = "NetworkInfo";
    static final String PRIMARY_DOMAIN = "wzrkt.com";
    static final String KEY_DOMAIN_NAME = "comms_dmn";
    static final String SPIKY_KEY_DOMAIN_NAME = "comms_dmn_spiky";
    static final String HEADER_DOMAIN_NAME = "X-WZRK-RD";
    static final String SPIKY_HEADER_DOMAIN_NAME = "X-WZRK-SPIKY-RD";
    static final String HEADER_MUTE = "X-WZRK-MUTE";
    static final String NAMESPACE_IJ = "IJ";
    static final String KEY_LAST_TS = "comms_last_ts";
    static final String KEY_FIRST_TS = "comms_first_ts";
    static final String KEY_I = "comms_i";
    static final String KEY_J = "comms_j";
    static final String CACHED_GUIDS_KEY = "cachedGUIDsKey";
    static final String MULTI_USER_PREFIX = "mt_";
    static final String NOTIFICATION_TAG = "wzrk_pn";
    static final String CHARGED_EVENT = "Charged";
    static final String FCM_PROPERTY_REG_ID = "fcm_token";
    static final String XPS_PROPERTY_REG_ID = "xps_token";
    static final String BPS_PROPERTY_REG_ID = "bps_token";
    static final String HPS_PROPERTY_REG_ID = "hps_token";
    static final String KEY_MUTED = "comms_mtd";
    static final int EMPTY_NOTIFICATION_ID = -1000;
    static final String KEY_MAX_PER_DAY = "istmcd_inapp";
    static final String KEY_COUNTS_SHOWN_TODAY = "istc_inapp";
    static final String KEY_COUNTS_PER_INAPP = "counts_per_inapp";
    static final String INAPP_ID_IN_PAYLOAD = "ti";
    static final int LOCATION_PING_INTERVAL_IN_SECONDS = 10;
    static final String[] SYSTEM_EVENTS = {NOTIFICATION_CLICKED_EVENT_NAME, NOTIFICATION_VIEWED_EVENT_NAME};
    static final long DEFAULT_PUSH_TTL = 1000 * 60 * 60 * 24 * 4;
    static final String PF_JOB_ID = "pfjobid";
    static final int PING_FREQUENCY_VALUE = 240;
    static final String PING_FREQUENCY = "pf";
    static final long ONE_MIN_IN_MILLIS = 60 * 1000L;
    static final String COPY_TYPE = "copy";
    static final String DND_START = "22:00";
    static final String DND_STOP = "06:00";
    static final String VIDEO_THUMBNAIL = "ct_video_1";
    static final String AUDIO_THUMBNAIL = "ct_audio";
    static final String IMAGE_PLACEHOLDER = "ct_image";
    //Keys used by the SDK
    static final String KEY_ACCOUNT_ID = "accountId";
    static final String KEY_ACCOUNT_TOKEN = "accountToken";
    static final String KEY_ACCOUNT_REGION = "accountRegion";
    static final String KEY_ANALYTICS_ONLY = "analyticsOnly";
    static final String KEY_DEFAULT_INSTANCE = "isDefaultInstance";
    static final String KEY_USE_GOOGLE_AD_ID = "useGoogleAdId";
    static final String KEY_DISABLE_APP_LAUNCHED = "disableAppLaunchedEvent";
    static final String KEY_PERSONALIZATION = "personalization";
    static final String KEY_DEBUG_LEVEL = "debugLevel";
    static final String KEY_CREATED_POST_APP_LAUNCH = "createdPostAppLaunch";
    static final String KEY_SSL_PINNING = "sslPinning";
    static final String KEY_BACKGROUND_SYNC = "backgroundSync";
    static final String KEY_FCM_SENDER_ID = "fcmSenderId";
    static final String KEY_CONFIG = "config";
    static final String KEY_C2A = "wzrk_c2a";
    static final String KEY_EFC = "efc";
    static final String KEY_TLC = "tlc";
    static final String KEY_TDC = "tdc";
    public static final String KEY_KV = "kv";
    public static final String KEY_TYPE = "type";
    static final String KEY_IS_TABLET = "tablet";
    public static final String KEY_BG = "bg";
    public static final String KEY_TITLE = "title";
    public static final String KEY_TEXT = "text";
    public static final String KEY_COLOR = "color";
    public static final String KEY_MESSAGE = "message";
    static final String KEY_HIDE_CLOSE = "close";
    public static final String KEY_MEDIA = "media";
    static final String KEY_MEDIA_LANDSCAPE = "mediaLandscape";
    static final String KEY_PORTRAIT = "hasPortrait";
    static final String KEY_LANDSCAPE = "hasLandscape";
    public static final String KEY_CONTENT_TYPE = "content_type";
    public static final String KEY_URL = "url";
    static final String KEY_BUTTONS = "buttons";
    static final String KEY_CUSTOM_HTML = "custom-html";
    static final String KEY_ENABLE_CUSTOM_CT_ID = "getEnableCustomCleverTapId";
    static final String KEY_BETA = "beta";
    static final String KEY_PACKAGE_NAME = "packageName";
    static final String KEY_ENABLE_UIEDITOR = "enableUIEditor";
    static final String KEY_ENABLE_ABTEST = "enableABTesting";
    static final String WZRK_PUSH_ID = "wzrk_pid";
    static final String NOTIF_MSG = "nm";
    static final String NOTIF_TITLE = "nt";
    static final String NOTIF_ICON = "ico";
    static final String WZRK_ACTIONS = "wzrk_acts";
    static final String WZRK_BIG_PICTURE = "wzrk_bp";
    static final String WZRK_MSG_SUMMARY = "wzrk_nms";
    static final String NOTIF_PRIORITY = "pr";
    static final String PRIORITY_HIGH = "high";
    static final String PRIORITY_MAX = "max";
    static final String WZRK_COLLAPSE = "wzrk_ck";
    static final String WZRK_CHANNEL_ID = "wzrk_cid";
    static final String WZRK_BADGE_ICON = "wzrk_bi";
    static final String WZRK_BADGE_COUNT = "wzrk_bc";
    static final String WZRK_SUBTITLE = "wzrk_st";
    static final String WZRK_COLOR = "wzrk_clr";
    static final String WZRK_SOUND = "wzrk_sound";
    static final String WZRK_TIME_TO_LIVE = "wzrk_ttl";
    static final String WZRK_RNV = "wzrk_rnv";
    static final String BLACK = "#000000";
    static final String WHITE = "#FFFFFF";
    static final String BLUE = "#0000FF";
    /**
     * Profile command constants.
     */
    static final String COMMAND_SET = "$set";
    static final String COMMAND_ADD = "$add";
    static final String COMMAND_REMOVE = "$remove";

    static final String COMMAND_DELETE = "$delete";
    static final String GUID_PREFIX_GOOGLE_AD_ID = "__g";
    static final String CUSTOM_CLEVERTAP_ID_PREFIX = "__h";
    static final String ERROR_PROFILE_PREFIX = "__i";
    // valid profile identifier keys
    static final Set<String> PROFILE_IDENTIFIER_KEYS = new HashSet<>(Arrays.asList(
            "Identity", "Email", "FBID", "GPID"));

    public static final String KEY_ICON = "icon";
    public static final String KEY_POSTER_URL = "poster";
    public static final String KEY_ACTION = "action";
    public static final String KEY_ANDROID = "android";
    public static final String KEY_ORIENTATION = "orientation";
    public static final String KEY_WZRK_PARAMS = "wzrkParams";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_CUSTOM_KV = "custom_kv";
    public static final String KEY_BORDER = "border";
    public static final String KEY_RADIUS = "radius";
    public static final String KEY_ACTIONS = "actions";
    public static final String KEY_ID = "id";
    public static final String KEY_DATE = "date";
    public static final String KEY_WZRK_TTL = "wzrk_ttl";
    public static final String KEY_IS_READ = "isRead";
    public static final String KEY_TAGS = "tags";
    public static final String KEY_MSG = "msg";
    public static final String KEY_HAS_URL = "hasUrl";
    public static final String KEY_HAS_LINKS = "hasLinks";
    public static final String KEY_LINKS = "links";
    public static final String TEST_IDENTIFIER = "0_0";
    public static final String FEATURE_DISPLAY_UNIT = "DisplayUnit : ";

    public static final String ACTION_CT_LOCAL_EVENT = "ACTION_CT_LOCAL_EVENT";
    public static final String KEY_CT_LOCAL_EVENT = "ACTION_CT_LOCAL_EVENT";
    public static final String CT_PUSH_AMP_SERVICE_START = "PushAmp Service Started";
    public static final String CT_PUSH_AMP_PING_EVENT_SENT = "PushAmp Ping Sent";
    public static final String CT_PUSH_AMP_PROCESS_RESPONSE = "PushAmp Ping Success";
    public static final String CT_PUSH_AMP_NOTIFICATION_CREATED = "PushAmp Notification Created";
    public static final String FEATURE_FLAG_UNIT = "Feature Flag : ";
    public static final String LOG_TAG_PRODUCT_CONFIG = "Product Config : ";
    public static final int FETCH_TYPE_PC = 0;
    public static final int FETCH_TYPE_FF = 1;

    public static final String LOG_TAG_GEOFENCES = "Geofences : ";
}