/**
 * File created on 26/06/14 at 20:54
 * Copyright Vyacheslav Krylov, 2014
 */
package im.actor.messenger.app.util;

import android.content.res.Resources;

import im.actor.messenger.app.AppContext;

public class Screen {
    private static float density;
    private static float scaledDensity;

    public static int dp(float dp) {
        if (density == 0f)
            density = AppContext.getContext().getResources().getDisplayMetrics().density;

        return (int) (dp * density + .5f);
    }

    public static int sp(float sp) {
        if (scaledDensity == 0f)
            scaledDensity = AppContext.getContext().getResources().getDisplayMetrics().scaledDensity;

        return (int) (sp * scaledDensity + .5f);
    }

    public static int getWidth() {
        return AppContext.getContext().getResources().getDisplayMetrics().widthPixels;
    }

    public static int getHeight() {
        return AppContext.getContext().getResources().getDisplayMetrics().heightPixels;
    }

    public static int getStatusBarHeight() {

        int result = 0;
        int resourceId = AppContext.getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = AppContext.getContext().getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getNavbarHeight() {
        if (hasNavigationBar()) {
            int resourceId = AppContext.getContext().getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return AppContext.getContext().getResources().getDimensionPixelSize(resourceId);
            }
        }
        return 0;
    }

    public static boolean hasNavigationBar() {
        Resources resources = AppContext.getContext().getResources();
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        return  (id > 0) && resources.getBoolean(id);
    }
}