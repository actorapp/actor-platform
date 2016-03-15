package im.actor.runtime;

public final class Log {

    private static final LogRuntime logRuntime = new LogRuntimeProvider();

    public static void w(String tag, String message) {
        if (!RuntimeEnvironment.isProduction()) {
            logRuntime.w(tag, message);
        }
    }

    public static void e(String tag, Throwable throwable) {
        logRuntime.e(tag, throwable);
        throwable.printStackTrace();
    }

    public static void d(String tag, String message) {
        if (!RuntimeEnvironment.isProduction()) {
            logRuntime.d(tag, message);
        }
    }

    public static void v(String tag, String message) {
        if (!RuntimeEnvironment.isProduction()) {
            logRuntime.v(tag, message);
        }
    }
}
