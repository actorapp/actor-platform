package im.actor.runtime.android;

import android.content.Context;

public final class AndroidContext {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        AndroidContext.context = context;
    }

    private AndroidContext() {
    }
}
