package im.actor.runtime;

/**
 * Created by ex3ndr on 08.08.15.
 */
public class LifecycleRuntimeProvider implements LifecycleRuntime {
    @Override
    public void killApp() {
        throw new RuntimeException("Dumb");
    }
}
