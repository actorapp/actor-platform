/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model;

import im.actor.model.jvm.JavaCryptoProvider;
import im.actor.model.jvm.JavaThreadingProvider;

public class BaseConfigurationBuilder extends ConfigurationBuilder {
    public BaseConfigurationBuilder() {
        setCryptoProvider(new JavaCryptoProvider());
        setThreadingProvider(new JavaThreadingProvider());
    }
}
