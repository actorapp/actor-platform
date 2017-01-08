package im.actor.sdk.util;

import android.os.Build;
import android.widget.AbsListView;
import android.widget.EdgeEffect;

import java.lang.reflect.Field;

import im.actor.runtime.Log;
import im.actor.runtime.android.AndroidContext;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;

/**
 * Created by 98379720172 on 05/01/17.
 */

public class AndroidUtils {

    private static Boolean isTablet = null;

    private static String TAG = AndroidUtils.class.getName();

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

    public static void setListViewEdgeEffectColor(AbsListView listView, int color) {
        if (Build.VERSION.SDK_INT >= 21) {
            try {
                Field field = AbsListView.class.getDeclaredField("mEdgeGlowTop");
                field.setAccessible(true);
                EdgeEffect mEdgeGlowTop = (EdgeEffect) field.get(listView);
                if (mEdgeGlowTop != null) {
                    mEdgeGlowTop.setColor(color);
                }

                field = AbsListView.class.getDeclaredField("mEdgeGlowBottom");
                field.setAccessible(true);
                EdgeEffect mEdgeGlowBottom = (EdgeEffect) field.get(listView);
                if (mEdgeGlowBottom != null) {
                    mEdgeGlowBottom.setColor(color);
                }
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        }
    }
}
