package im.actor.runtime;

import im.actor.runtime.android.AndroidLogProvider;

public class LogRuntimeProvider extends AndroidLogProvider {

    @Override
    public void w(String tag, String message) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public void e(String tag, Throwable throwable) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public void d(String tag, String message) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public void v(String tag, String message) {
        throw new RuntimeException("Dumb");
    }
}
