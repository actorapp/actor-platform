package im.actor.runtime.cocoa;

import im.actor.runtime.LifecycleRuntime;

public class CocoaLifecycleProvider implements LifecycleRuntime {
    @Override
    public native void killApp()/*-[
        @throw NSInternalInconsistencyException;
    ]-*/;
}
