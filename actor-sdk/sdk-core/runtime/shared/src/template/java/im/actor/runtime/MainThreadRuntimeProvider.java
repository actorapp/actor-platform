package im.actor.runtime;

/**
 * Created by ex3ndr on 07.08.15.
 */
public class MainThreadRuntimeProvider implements MainThreadRuntime {
    @Override
    public void postToMainThread(Runnable runnable) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public boolean isMainThread() {
        throw new RuntimeException("Dumb");
    }

    @Override
    public boolean isSingleThread() {
        throw new RuntimeException("Dumb");
    }
}
