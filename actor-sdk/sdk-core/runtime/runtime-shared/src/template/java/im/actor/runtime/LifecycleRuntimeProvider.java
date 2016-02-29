package im.actor.runtime;

import im.actor.runtime.power.WakeLock;

public class LifecycleRuntimeProvider implements LifecycleRuntime {
    @Override
    public void killApp() {
        throw new RuntimeException("Dumb");
    }

    @Override
    public WakeLock makeWakeLock() {
        throw new RuntimeException("Dumb");
    }
}
