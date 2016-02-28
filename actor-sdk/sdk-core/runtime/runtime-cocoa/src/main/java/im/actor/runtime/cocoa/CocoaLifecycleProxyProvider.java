package im.actor.runtime.cocoa;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.LifecycleRuntime;
import im.actor.runtime.power.WakeLock;

public class CocoaLifecycleProxyProvider implements LifecycleRuntime {

    private static LifecycleRuntime lifecycleRuntime;

    @ObjectiveCName("setLifecycleRuntime:")
    public static void setLifecycleRuntime(LifecycleRuntime lifecycleRuntime) {
        CocoaLifecycleProxyProvider.lifecycleRuntime = lifecycleRuntime;
    }

    @Override
    public void killApp() {
        if (lifecycleRuntime == null) {
            throw new RuntimeException("Lifecycle Runtime not set!");
        }
        lifecycleRuntime.killApp();
    }

    @Override
    public WakeLock makeWakeLock() {
        if (lifecycleRuntime == null) {
            throw new RuntimeException("Lifecycle Runtime not set!");
        }
        return lifecycleRuntime.makeWakeLock();
    }
}
