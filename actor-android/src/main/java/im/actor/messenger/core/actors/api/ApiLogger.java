package im.actor.messenger.core.actors.api;

import android.util.Log;

import im.actor.api.LogInterface;

/**
 * Created by ex3ndr on 16.11.14.
 */
public class ApiLogger implements LogInterface {
    @Override
    public void d(String tag, String message) {
        Log.d(tag, message);
    }

    @Override
    public void w(String tag, String message) {
        Log.w(tag, message);
    }

    @Override
    public void e(String tag, Throwable throwable) {
        Log.e(tag, "", throwable);
    }
}
