/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.log;

import im.actor.model.LogProvider;

public class Log {

    private static LogProvider log;

    public static LogProvider getLog() {
        return log;
    }

    public static void setLog(LogProvider log) {
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
