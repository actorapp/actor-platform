package im.actor.runtime.power;

import com.google.j2objc.annotations.ObjectiveCName;

public interface WakeLock {

    // Don't use "release" prefix as it is conflicts with ObjC runtime
    @ObjectiveCName("closeLock")
    void releaseLock();
}
