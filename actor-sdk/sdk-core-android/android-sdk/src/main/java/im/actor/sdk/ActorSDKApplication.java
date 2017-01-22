package im.actor.sdk;

import android.app.ActivityManager;
import android.app.Application;
import android.content.res.Configuration;

import im.actor.runtime.android.AndroidContext;
import im.actor.sdk.util.AndroidUtils;
import im.actor.sdk.util.Screen;

/**
 * Implementation of Application object that handles everything required for creating and
 * managing Actor SDK
 */
public class ActorSDKApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        int id = android.os.Process.myPid();
        String myProcessName = getPackageName();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo procInfo : activityManager.getRunningAppProcesses()) {
            if (id == procInfo.pid) {
                myProcessName = procInfo.processName;
            }
        }

        // Protection on double start
        if (!myProcessName.endsWith(":actor_push")) {
            AndroidContext.setContext(this);
            onConfigureActorSDK();
            ActorSDK.sharedActor().createActor(this);
        }
    }

    /**
     * Override this method for implementing Actor SDK Implementation
     */
    public void onConfigureActorSDK() {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            Screen.checkDisplaySize(getApplicationContext(), newConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}