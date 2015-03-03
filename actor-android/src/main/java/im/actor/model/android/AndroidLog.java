package im.actor.model.android;

import android.util.Log;

import im.actor.model.LogCallback;

/**
 * Created by ex3ndr on 16.02.15.
 */
public class AndroidLog implements LogCallback {
    @Override
    public void w(String tag, String message) {
        Log.w(tag, message);
    }

    @Override
    public void e(String tag, Throwable throwable) {
        Log.e(tag, "", throwable);
    }

    @Override
    public void d(String tag, String message) {
        Log.d(tag, message);
    }

    @Override
    public void v(String tag, String message) {
        Log.v(tag, message);
    }
}
