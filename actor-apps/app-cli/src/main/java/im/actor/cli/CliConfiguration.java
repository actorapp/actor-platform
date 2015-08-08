package im.actor.cli;

import im.actor.cli.providers.CliDispatcherProvider;
import im.actor.cli.providers.CliLifecycleProvider;
import im.actor.cli.providers.CliMainThreadProvider;
import im.actor.core.ApiConfiguration;
import im.actor.core.AppCategory;
import im.actor.core.ConfigurationBuilder;
import im.actor.core.Messenger;
import im.actor.core.runtime.crypto.bouncycastle.BouncyCastleRuntime;
import im.actor.core.jvm.JvmLocale;
import im.actor.core.mem.MemoryStorageProvider;
import im.actor.core.providers.EmptyPhoneProvider;
import im.actor.core.runtime.JavaRandomProvider;
import im.actor.core.tcp.TcpNetworkProvider;

/**
 * Created by ex3ndr on 29.07.15.
 */
public class CliConfiguration {
    public static ConfigurationBuilder create() {

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setAppCategory(AppCategory.GENERIC);
        configurationBuilder.setNetworkProvider(new TcpNetworkProvider());
        configurationBuilder.setLocaleProvider(new JvmLocale("En"));
        configurationBuilder.setPhoneBookProvider(new EmptyPhoneProvider());
        configurationBuilder.setCryptoProvider(new BouncyCastleRuntime(new JavaRandomProvider()));
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
