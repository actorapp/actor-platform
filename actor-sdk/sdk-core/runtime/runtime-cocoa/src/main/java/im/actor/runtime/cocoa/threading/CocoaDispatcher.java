package im.actor.runtime.cocoa.threading;

import im.actor.runtime.threading.Dispatcher;

public class CocoaDispatcher implements Dispatcher {

    @Override
    public void dispatch(Runnable message, long delay) {
        dispatchCocoa(message, delay);
    }

    private native void dispatchCocoa(Runnable runnable, long delay)/*-[
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, delay * NSEC_PER_MSEC), dispatch_get_global_queue( DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            [runnable run];
        });
    ]-*/;
}
