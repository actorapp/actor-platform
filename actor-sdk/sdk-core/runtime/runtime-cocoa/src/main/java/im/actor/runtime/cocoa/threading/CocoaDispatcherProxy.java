package im.actor.runtime.cocoa.threading;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.threading.DispatchCancel;

public interface CocoaDispatcherProxy {
    @ObjectiveCName("dispatchOnBackground:withDelay:")
    DispatchCancel dispatchOnBackground(Runnable runnable, long delay);
}
