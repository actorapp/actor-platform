package im.actor.cli.providers;

import im.actor.model.DispatcherProvider;

/**
 * Created by ex3ndr on 29.07.15.
 */
public class CliDispatcherProvider implements DispatcherProvider {
    @Override
    public void dispatch(Runnable runnable) {
        runnable.run();
    }
}
