/**
 * File created on 26/06/14 at 20:54
 * Copyright Vyacheslav Krylov, 2014
 */
package im.actor.messenger.app.util;

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
}