package im.actor.model.jvm;

import im.actor.model.BaseConfigurationBuilder;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class JvmConfigurationBuilder extends BaseConfigurationBuilder {
    public JvmConfigurationBuilder() {
        setLog(new JavaLog());
        setNetworkProvider(new JavaNetworkProvider());
        setLocale(new JavaLocale("En"));
    }
}
