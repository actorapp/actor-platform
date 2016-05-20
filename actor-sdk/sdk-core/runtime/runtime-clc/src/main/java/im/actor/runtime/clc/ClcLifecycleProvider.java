package im.actor.runtime.clc;

import im.actor.runtime.LifecycleRuntime;
import im.actor.runtime.power.WakeLock;

/**
 * Created by amir on 3/14/16.
 */
public class ClcLifecycleProvider implements LifecycleRuntime {

    @Override
    public void killApp() {
    }

    @Override
    public WakeLock makeWakeLock() {
        return new ClcWakeLock();
    }
}
