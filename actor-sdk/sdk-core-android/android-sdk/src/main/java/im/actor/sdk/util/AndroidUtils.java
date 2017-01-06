package im.actor.sdk.util;

import im.actor.runtime.android.AndroidContext;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;

/**
 * Created by 98379720172 on 05/01/17.
 */

public class AndroidUtils {

    private static Boolean isTablet = null;

    public static boolean isTablet() {
        if (isTablet == null) {
            isTablet = AndroidContext.getContext().getResources().getBoolean(R.bool.isTablet);
        }
        return isTablet;
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (delay == 0) {
            AndroidContext.applicationHandler().post(runnable);
        } else {
            AndroidContext.applicationHandler().postDelayed(runnable, delay);
        }
    }

    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }
}
