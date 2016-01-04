package im.actor.app;

import im.actor.core.ConfigurationBuilder;
import im.actor.core.JavaSeMessenger;

public class Main {

    public static void main(String[] args) {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        // TODO: Perform required configuration
        JavaSeMessenger messenger = new JavaSeMessenger(builder.build());
        // TODO: Start working with messenger object
    }
}
