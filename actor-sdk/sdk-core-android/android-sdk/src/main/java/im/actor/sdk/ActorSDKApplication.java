package im.actor.sdk;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;

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
            onConfigureActorSDK();
            ActorSDK.sharedActor().createActor(this);
            ActorSDK.sharedActor().setComponentName(getPackageManager().getLaunchIntentForPackage(getPackageName()).getComponent());
        }
    }

    /**
     * Override this method for implementing Actor SDK Implementation
     */
    public void onConfigureActorSDK() {

    }
}