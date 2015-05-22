/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.jvm;

import im.actor.model.BaseConfigurationBuilder;

public class JvmConfigurationBuilder extends BaseConfigurationBuilder {

    public JvmConfigurationBuilder(String locale) {
        setLogProvider(new JavaLog());
        setNetworkProvider(new JavaNetworkProvider());
        setLocaleProvider(new JavaLocale(locale));
    }
}
