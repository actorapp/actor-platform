package im.actor.cli.providers;

import im.actor.model.MainThreadProvider;

/**
 * Created by ex3ndr on 29.07.15.
 */
public class CliMainThreadProvider implements MainThreadProvider {

    @Override
    public void postToMainThread(Runnable runnable) {
        runnable.run();
    }

    @Override
    public boolean isMainThread() {
        return true;
    }

    @Override
    public boolean isSingleThread() {
        return false;
    }
}
