package im.actor.runtime.cocoa;

import im.actor.runtime.LogRuntime;

public class CocoaLogProvider implements LogRuntime {

    @Override
    public native void w(String tag, String message)/*-[
        NSLog(@"WARRING %@: %@", tag, message);
    ]-*/;

    @Override
    public native void e(String tag, Throwable throwable)/*-[
        NSLog(@"ERROR %@:", tag);
        [throwable printStackTrace];
    ]-*/;


    @Override
    public native void d(String tag, String message)/*-[
        NSLog(@"%@: %@", tag, message);
    ]-*/;

    @Override
    public native void v(String tag, String message)/*-[
        NSLog(@"%@: %@", tag, message);
    ]-*/;
}
