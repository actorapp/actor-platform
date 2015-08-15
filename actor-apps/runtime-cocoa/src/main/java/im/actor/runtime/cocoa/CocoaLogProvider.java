package im.actor.runtime.cocoa;

import java.util.logging.Level;
import java.util.logging.Logger;

import im.actor.runtime.LogRuntime;

public class CocoaLogProvider implements LogRuntime {

    private final Logger logger = Logger.getGlobal();

    @Override
    public void w(String tag, String message) {
        logger.warning(tag + ": " + message);
    }

    @Override
    public void e(String tag, Throwable throwable) {
        logger.log(Level.SEVERE, tag + ": " + throwable.getMessage(), throwable);
    }

    @Override
    public native void d(String tag, String message)/*-[
        NSLog(@"%@: %@", tag, message);
    ]-*/;

    @Override
    public native void v(String tag, String message)/*-[
        NSLog(@"%@: %@", tag, message);
    ]-*/;

//    @Override
//    public void d(String tag, String message) {
//        logger.log(Level.FINE, tag + ": " + message);
//    }

//    @Override
//    public void v(String tag, String message) {
//        logger.log(Level.FINER, tag + ": " + message);
//    }
}
