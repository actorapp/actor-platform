package im.actor.runtime.js.power;

import im.actor.runtime.power.WakeLock;

public class JsWakeLock implements WakeLock {

    @Override
    public void releaseLock() {
        // Do nothing as wake lock is not required for js environments
    }
}
