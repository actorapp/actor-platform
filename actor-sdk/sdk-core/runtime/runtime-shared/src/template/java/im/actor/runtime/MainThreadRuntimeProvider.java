package im.actor.runtime;

import im.actor.runtime.os.OSType;

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

    @Override
    public OSType getOSType() {
        throw new RuntimeException("Dumb");
    }
}
