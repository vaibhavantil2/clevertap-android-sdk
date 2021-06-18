package com.clevertap.android.sdk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import java.util.HashSet;

/**
 * Class for handling activity lifecycle events
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class ActivityLifecycleCallback {

    public static boolean registered = false;

    private static HashSet<String> excludedActivitiesSet = null;

    /**
     * Enables lifecycle callbacks for Android devices
     *
     * @param application App's Application object
     * @param cleverTapID Custom CleverTap ID
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static synchronized void register(android.app.Application application, final String cleverTapID) {
        if (application == null) {
            Logger.i("Application instance is null/system API is too old");
            return;
        }

        if (registered) {
            Logger.v("Lifecycle callbacks have already been registered");
            return;
        }

        updateBlacklistedActivitySet(application.getApplicationContext());

        registered = true;
        application.registerActivityLifecycleCallbacks(
                new android.app.Application.ActivityLifecycleCallbacks() {

                    @Override
                    public void onActivityCreated(Activity activity, Bundle bundle) {
                        if (canProcessLifeCycleCallbacksOnActivity(activity)) {
                            if (cleverTapID != null) {
                                CleverTapAPI.onActivityCreated(activity, cleverTapID);
                            } else {
                                CleverTapAPI.onActivityCreated(activity);
                            }
                        } else {
                            Logger.v("Not running onActivityCreated for - " + activity.getLocalClassName());
                        }
                    }

                    @Override
                    public void onActivityDestroyed(Activity activity) {
                    }

                    @Override
                    public void onActivityPaused(Activity activity) {
                        CleverTapAPI.onActivityPaused();
                    }

                    @Override
                    public void onActivityResumed(Activity activity) {
                        if (canProcessLifeCycleCallbacksOnActivity(activity)) {
                            if (cleverTapID != null) {
                                CleverTapAPI.onActivityResumed(activity, cleverTapID);
                            } else {
                                CleverTapAPI.onActivityResumed(activity);
                            }
                        } else {
                            Logger.v("Not running onActivityResumed for - " + activity.getLocalClassName());
                        }
                    }

                    @Override
                    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
                    }

                    @Override
                    public void onActivityStarted(Activity activity) {
                    }

                    @Override
                    public void onActivityStopped(Activity activity) {
                    }
                }

        );
        Logger.i("Activity Lifecycle Callback successfully registered");
    }

    /**
     * Enables lifecycle callbacks for Android devices
     *
     * @param application App's Application object
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static synchronized void register(android.app.Application application) {
        register(application, null);
    }

    private static boolean canProcessLifeCycleCallbacksOnActivity(Activity activity) {
        for (String blacklistedActivity : excludedActivitiesSet) {
            if (activity != null && activity.getLocalClassName().contains(blacklistedActivity)) {
                return false;
            }
        }
        return true;
    }

    private static void updateBlacklistedActivitySet(Context context) {
        if (excludedActivitiesSet == null) {
            excludedActivitiesSet = new HashSet<>();
            try {
                String activities = ManifestInfo.getInstance(context).getExcludedActivitiesForLifecycleMethods();
                if (activities != null) {
                    String[] split = activities.split(",");
                    for (String a : split) {
                        excludedActivitiesSet.add(a.trim());
                        Logger.v("Excluding following activity - " + a.trim());
                    }
                }
            } catch (Throwable t) {
                // Ignore
            }
        }
    }
}
