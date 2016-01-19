package im.actor.app;

import im.actor.core.ApiConfiguration;
import im.actor.core.ConfigurationBuilder;
import im.actor.core.JavaSeMessenger;
import im.actor.core.PhoneBookProvider;

public class Main {

    public static void main(String[] args) {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addEndpoint("tcp://front1-mtproto-api-rev3.actor.im:443");
        builder.addEndpoint("tcp://front2-mtproto-api-rev3.actor.im:443");
        builder.setPhoneBookProvider(new PhoneBookProvider() {
            @Override
            public void loadPhoneBook(Callback callback) {

            }
        });
        builder.setPhoneBookImportEnabled(false);
        builder.setApiConfiguration(new ApiConfiguration("", 0,
                "", "", ""));
        JavaSeMessenger messenger = new JavaSeMessenger(builder.build());
        // TODO: Start working with messenger object
    }
}
