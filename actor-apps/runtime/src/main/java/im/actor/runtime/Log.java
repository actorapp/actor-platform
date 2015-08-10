package im.actor.runtime;

public final class Log {

    private static final LogRuntime log = Runtime.getLogRuntime();

    public static void w(String tag, String message) {
        log.w(tag, message);
    }

    public static void e(String tag, Throwable throwable) {
        log.e(tag, throwable);
    }

    public static void d(String tag, String message) {
        log.d(tag, message);
    }

    public static void v(String tag, String message) {
        log.v(tag, message);
    }
}
