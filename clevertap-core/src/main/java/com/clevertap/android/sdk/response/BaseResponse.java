package com.clevertap.android.sdk.response;

import android.content.Context;
import com.clevertap.android.sdk.CleverTapInstanceConfig;
import com.clevertap.android.sdk.DeviceInfo;
import com.clevertap.android.sdk.LocalDataStore;
import com.clevertap.android.sdk.Logger;
import com.clevertap.android.sdk.network.NetworkManager;
import org.json.JSONObject;

public class BaseResponse extends CleverTapResponseDecorator {

    private final CleverTapResponse mCleverTapResponse;

    private final CleverTapInstanceConfig mConfig;

    private final Context mContext;

    private final DeviceInfo mDeviceInfo;

    private final LocalDataStore mLocalDataStore;

    private final Logger mLogger;

    private final NetworkManager mNetworkManager;

    public BaseResponse(Context context, CleverTapInstanceConfig config,
            DeviceInfo deviceInfo, NetworkManager networkManager, LocalDataStore localDataStore,
            CleverTapResponse cleverTapResponse) {
        mCleverTapResponse = cleverTapResponse;

        mContext = context;
        mConfig = config;
        mDeviceInfo = deviceInfo;
        mLogger = mConfig.getLogger();

        mNetworkManager = networkManager;
        mLocalDataStore = localDataStore;

    }

    @Override
    public void processResponse(final JSONObject jsonBody, final String responseStr, final Context context) {

        if (responseStr == null) {
            mLogger.verbose(mConfig.getAccountId(), "Problem processing queue response, response is null");
            return;
        }
        try {
            mLogger.verbose(mConfig.getAccountId(), "Trying to process response: " + responseStr);

            JSONObject response = new JSONObject(responseStr);
            // in app
            mCleverTapResponse.processResponse(response, responseStr, context);

            try {
                mLocalDataStore.syncWithUpstream(context, response);
            } catch (Throwable t) {
                mLogger.verbose(mConfig.getAccountId(), "Failed to sync local cache with upstream", t);
            }

        } catch (Throwable t) {
            mNetworkManager.incrementResponseFailureCount();
            mLogger.verbose(mConfig.getAccountId(), "Problem process send queue response", t);
        }
    }
}