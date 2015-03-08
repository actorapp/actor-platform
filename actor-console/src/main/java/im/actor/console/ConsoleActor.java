package im.actor.console;

import java.util.HashMap;

import im.actor.model.ApiConfiguration;
import im.actor.model.ConfigurationBuilder;
import im.actor.model.LocaleProvider;
import im.actor.model.MainThread;
import im.actor.model.Messenger;
import im.actor.model.PhoneBookProvider;
import im.actor.model.crypto.bouncycastle.BouncyCastleProvider;
import im.actor.model.jvm.JavaLog;
import im.actor.model.jvm.JavaNetworking;
import im.actor.model.jvm.JavaThreading;
import im.actor.model.log.Log;
import im.actor.model.storage.temp.TempStorage;

/**
 * Created by ex3ndr on 08.03.15.
 */
public class ConsoleActor {

    private static Messenger messenger;

    public static void connect() {
        connect("tcp://mtproto-api.actor.im:8080");
    }

    public static void connect(String url) {
        if (messenger != null) {
            Log.d("ConsoleActor", "Already created");
        }

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setNetworking(new JavaNetworking());
        builder.setPhoneBookProvider(new PhoneBookProvider() {
            @Override
            public void loadPhoneBook(Callback callback) {

            }
        });
        builder.setApiConfiguration(new ApiConfiguration("Actor Console",
                1, "???", "Some console", new byte[0]));
        builder.setMainThread(new MainThread() {
            @Override
            public void runOnUiThread(Runnable runnable) {
                runnable.run();
            }
        });
        builder.setStorage(new TempStorage());
        builder.setCryptoProvider(new BouncyCastleProvider());
        builder.setLocale(new LocaleProvider() {
            @Override
            public HashMap<String, String> loadLocale() {
                return new HashMap<String, String>();
            }
        });
        builder.setLog(new JavaLog());
        builder.setThreading(new JavaThreading());
        builder.addEndpoint(url);

        messenger = new Messenger(builder.build());
    }

    public static Messenger getMessenger() {
        return messenger;
    }
}
