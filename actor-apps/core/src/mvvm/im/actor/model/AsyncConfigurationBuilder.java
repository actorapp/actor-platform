/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.model.runtime.JavaCryptoProvider;
import im.actor.model.runtime.JavaThreadingProvider;

public class AsyncConfigurationBuilder extends ConfigurationBuilder {

    @ObjectiveCName("init")
    public AsyncConfigurationBuilder() {
        setCryptoProvider(new JavaCryptoProvider());
        setThreadingRuntime(new JavaThreadingProvider());
    }
}
