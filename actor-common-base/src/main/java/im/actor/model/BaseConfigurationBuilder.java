package im.actor.model;

import im.actor.model.jvm.JavaCryptoProvider;
import im.actor.model.jvm.JavaThreadingProvider;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class BaseConfigurationBuilder extends ConfigurationBuilder {
    public BaseConfigurationBuilder() {
        setCryptoProvider(new JavaCryptoProvider());
        setThreadingProvider(new JavaThreadingProvider());
    }
}
