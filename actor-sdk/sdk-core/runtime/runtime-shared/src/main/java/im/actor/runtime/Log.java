package im.actor.runtime;

public final class Log {

    private static final LogRuntime logRuntime = new LogRuntimeProvider();

    public static void w(String tag, String message) {
        logRuntime.w(tag, message);
    }

    public static void e(String tag, Throwable throwable) {
        logRuntime.e(tag, throwable);
    }

    public static void d(String tag, String message) {
        logRuntime.d(tag, message);
    }

    public static void v(String tag, String message) {
        logRuntime.v(tag, message);
    }
}
