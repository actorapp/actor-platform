package im.actor.runtime;

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
