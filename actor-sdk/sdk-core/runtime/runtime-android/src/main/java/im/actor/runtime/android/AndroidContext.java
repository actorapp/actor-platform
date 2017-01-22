package im.actor.runtime.android;

import android.content.Context;
import android.os.Handler;

public final class AndroidContext {

    private static Context context;
    private static volatile Handler applicationHandler;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        AndroidContext.context = context;
    }

    public static Handler applicationHandler(){
        if(applicationHandler == null)
            applicationHandler = new Handler(getContext().getMainLooper());

        return applicationHandler;
    }

    private AndroidContext() {
    }
}
