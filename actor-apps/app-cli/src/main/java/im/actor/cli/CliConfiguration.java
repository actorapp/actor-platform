package im.actor.cli;

import im.actor.cli.providers.CliDispatcherProvider;
import im.actor.cli.providers.CliLifecycleProvider;
import im.actor.cli.providers.CliMainThreadProvider;
import im.actor.model.ApiConfiguration;
import im.actor.model.AppCategory;
import im.actor.model.ConfigurationBuilder;
import im.actor.model.Messenger;
import im.actor.model.crypto.bouncycastle.BouncyCastleProvider;
import im.actor.model.jvm.JavaRandomProvider;
import im.actor.model.jvm.JavaThreadingProvider;
import im.actor.model.jvm.JvmLocale;
import im.actor.model.mem.MemoryStorageProvider;
import im.actor.model.providers.EmptyPhoneProvider;
import im.actor.model.tcp.TcpNetworkProvider;

/**
 * Created by ex3ndr on 29.07.15.
 */
public class CliConfiguration {
    public static ConfigurationBuilder create() {

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setAppCategory(AppCategory.GENERIC);
        configurationBuilder.setThreadingProvider(new JavaThreadingProvider());
        configurationBuilder.setNetworkProvider(new TcpNetworkProvider());
        configurationBuilder.setLocaleProvider(new JvmLocale("En"));
        configurationBuilder.setPhoneBookProvider(new EmptyPhoneProvider());
        configurationBuilder.setCryptoProvider(new BouncyCastleProvider(new JavaRandomProvider()));
        configurationBuilder.setStorageProvider(new MemoryStorageProvider());

        configurationBuilder.setMainThreadProvider(new CliMainThreadProvider());
        configurationBuilder.setLifecycleProvider(new CliLifecycleProvider());
        configurationBuilder.setDispatcherProvider(new CliDispatcherProvider());

        configurationBuilder.setApiConfiguration(new ApiConfiguration("Actor CLI", 0, "", "", ""));

        return configurationBuilder;
    }

    public static Messenger createMessenger(String url) {
        ConfigurationBuilder configurationBuilder = CliConfiguration.create();
        configurationBuilder.addEndpoint(url);
        return new Messenger(configurationBuilder.build());
    }
}
