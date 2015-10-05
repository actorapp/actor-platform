package im.actor.runtime.cocoa;

import im.actor.runtime.MainThreadRuntime;

public class CocoaMainThreadProvider implements MainThreadRuntime {

    @Override
    public native void postToMainThread(Runnable runnable)/*-[
        dispatch_async(dispatch_get_main_queue(), ^{
            // NSLog(@"dispatchOnUi(core) %@", runnable);
            [runnable run];
        });
    ]-*/;

    @Override
    public native boolean isMainThread()/*-[
        return [NSThread currentThread].isMainThread;
    ]-*/;

    @Override
    public boolean isSingleThread() {
        return false;
    }
}
