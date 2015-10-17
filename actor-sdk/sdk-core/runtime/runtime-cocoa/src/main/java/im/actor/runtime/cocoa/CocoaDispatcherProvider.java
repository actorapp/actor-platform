package im.actor.runtime.cocoa;

import im.actor.runtime.DispatcherRuntime;

public class CocoaDispatcherProvider implements DispatcherRuntime {

    @Override
    public native void dispatch(Runnable runnable)/*-[
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), ^{
            [runnable run];
        });
    ]-*/;
}
