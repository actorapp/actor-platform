package im.actor.runtime;

/**
 * Created by ex3ndr on 07.08.15.
 */
public class LogRuntimeProvider implements LogRuntime {

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
