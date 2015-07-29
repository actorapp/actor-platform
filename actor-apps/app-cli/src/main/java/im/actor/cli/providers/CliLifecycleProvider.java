package im.actor.cli.providers;

import im.actor.model.LifecycleProvider;

/**
 * Created by ex3ndr on 29.07.15.
 */
public class CliLifecycleProvider implements LifecycleProvider {
    @Override
    public void killApp() {
        Runtime.getRuntime().halt(1);
    }
}
