package im.actor.runtime;

public class DispatcherRuntimeProvider implements DispatcherRuntime {
    @Override
    public void dispatch(Runnable runnable) {
        throw new RuntimeException("Dumb");
    }
}
