package im.actor.runtime.cocoa.threading;

public interface CocoaDispatcherProxy {
    void dispatchOnBackground(Runnable runnable, long delay);
}
