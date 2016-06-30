package im.actor.runtime.power;

import com.google.j2objc.annotations.ObjectiveCName;

public interface WakeLock {

    @ObjectiveCName("releaseLock")
    void releaseLock();
}
