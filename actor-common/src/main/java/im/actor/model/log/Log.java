package im.actor.model.log;

import im.actor.model.LogCallback;

/**
 * Created by ex3ndr on 06.02.15.
 */
public class Log {

    private static LogCallback log;

    public static LogCallback getLog() {
        return log;
    }

    public static void setLog(LogCallback log) {
        Log.log = log;
    }

    public static void w(String tag, String message) {
        if (log != null) {
            log.w(tag, message);
        }
    }

    public static void e(String tag, Throwable throwable) {
        if (log != null) {
            log.e(tag, throwable);
        }
    }

    public static void d(String tag, String message) {
        if (log != null) {
            log.d(tag, message);
        }
    }

    public static void v(String tag, String message) {
        if (log != null) {
            log.v(tag, message);
        }
    }
}
