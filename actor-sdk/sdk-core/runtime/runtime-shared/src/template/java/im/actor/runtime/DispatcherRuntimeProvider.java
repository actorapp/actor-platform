package im.actor.runtime;

/**
 * Created by ex3ndr on 07.08.15.
 */
public class DispatcherRuntimeProvider implements DispatcherRuntime {
    @Override
    public void dispatch(Runnable runnable) {
        throw new RuntimeException("Dumb");
    }
}
