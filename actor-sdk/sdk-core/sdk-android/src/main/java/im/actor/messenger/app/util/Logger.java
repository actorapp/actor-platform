package im.actor.messenger.app.util;

import android.util.Log;

import java.util.ArrayList;

public class Logger {
    public static final String TAG = "SECRET";

//    private static ActorRef logActor = ActorSystem.system().actorOf(LogActor.class, "log");

    public static void v(String msg, Object... args) {
        v(TAG, msg, args);
    }

    public static void v(Throwable t) {
        v(TAG, "Exception thrown", t);
    }

    public static void v(String msg, Throwable t) {
        v(TAG, msg, t);
    }

    public static void v(String tag, String msg, Throwable t) {
        print(Log.VERBOSE, tag, msg, t);
    }

    public static void v(String tag, String msg, Object... args) {
        msg = format(msg, args);
        print(Log.VERBOSE, tag, msg);
    }

    public static void i(String msg, Object... args) {
        i(TAG, msg, args);
    }

    public static void i(String tag, String msg, Object... args) {
        msg = format(msg, args);
        print(Log.INFO, tag, msg);
    }

    public static void d(String msg, Object... args) {
        d(TAG, msg, args);
    }

    public static void d(Throwable t) {
        d(TAG, "Exception thrown", t);
    }

    public static void d(String tag, String msg, Object... args) {
        msg = format(msg, args);
        print(Log.DEBUG, tag, msg);
    }

    public static void d(String tag, String msg, Throwable t) {
        print(Log.DEBUG, tag, msg, t);
    }

    public static void e(String msg, Throwable t) {
        e(TAG, msg, t);
    }

    public static void e(Throwable t) {
        e(TAG, "Exception thrown", t);
    }

    public static void e(String tag, String msg, Object... args) {
        msg = format(msg, args);
        print(Log.ERROR, tag, msg);
    }

    public static void e(String tag, String msg, Throwable t) {
        print(Log.ERROR, tag, msg, t);
    }

    public static void w(String msg, Object... args) {
        w(TAG, msg, args);
    }

    public static void w(Throwable t) {
        w(TAG, "Exception thrown", t);
    }

    public static void w(String msg, Throwable t) {
        w(TAG, msg, t);
    }

    public static void w(String tag, String msg, Object... args) {
        msg = format(msg, args);
        print(Log.WARN, tag, msg);
    }

    public static void w(String tag, String msg, Throwable t) {
        print(Log.WARN, tag, msg, t);
    }

    private static String format(String str, Object[] args) {
        return args.length > 0 ? String.format(str, args) : str;
    }

    private static void print(int level, String tag, String msg) {
        print(level, tag, msg, null);
    }

    private static void print(int level, String tag, String msg, Throwable throwable) {
//        if (CurrentActor.getCurrentActor() != null) {
//            logActor.send(new LogRecord(level, tag, msg, throwable), CurrentActor.getCurrentActor().self());
//        } else {
//            logActor.send(new LogRecord(level, tag, msg, throwable));
//        }
    }

    private static ArrayList<String> log = new ArrayList<String>();

    public static String rawLog() {
        synchronized (log) {
            String res = "";
            for (String s : log) {
                res += s + "<br/>";
            }
            return res;
        }
    }

//    public static class LogActor extends Actor {
//
//        @Override
//        public void onReceive(Object message) {
//            if (message instanceof LogRecord) {
//                LogRecord record = (LogRecord) message;
//                synchronized (log) {
//                    log.add(record.tag + ":" + record.msg + ":" + record.throwable);
//                }
//                onReceive(record.level, record.tag, record.msg, record.throwable);
//            }
//        }
//
//        public void onReceive(int level, String tag, String message, Throwable throwable) {
//            switch (level) {
//                case Log.DEBUG: {
//                    if (throwable != null)
//                        Log.d(tag, message, throwable);
//                    else
//                        Log.d(tag, message);
//
//                    break;
//                }
//                case Log.ERROR: {
//                    if (throwable != null)
//                        Log.e(tag, message, throwable);
//                    else
//                        Log.e(tag, message);
//
//                    break;
//                }
//                case Log.INFO: {
//                    if (throwable != null)
//                        Log.i(tag, message, throwable);
//                    else
//                        Log.i(tag, message);
//
//                    break;
//                }
//                case Log.WARN: {
//                    if (throwable != null)
//                        Log.w(tag, message, throwable);
//                    else
//                        Log.w(tag, message);
//
//                    break;
//                }
//                case Log.VERBOSE:
//                default: {
//                    if (throwable != null)
//                        Log.v(tag, message, throwable);
//                    else
//                        Log.v(tag, message);
//
//                    break;
//                }
//            }
//        }
//    }

    static class LogRecord {
        final int level;
        final String tag;
        final String msg;
        final Throwable throwable;

        LogRecord(int level, String tag, String msg, Throwable throwable) {
            this.level = level;
            this.tag = tag;
            this.msg = msg;
            this.throwable = throwable;
        }
    }
}
