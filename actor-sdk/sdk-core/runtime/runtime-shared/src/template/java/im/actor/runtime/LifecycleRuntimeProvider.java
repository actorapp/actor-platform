package im.actor.runtime;

public class LifecycleRuntimeProvider implements LifecycleRuntime {
    @Override
    public void killApp() {
        throw new RuntimeException("Dumb");
    }
}
