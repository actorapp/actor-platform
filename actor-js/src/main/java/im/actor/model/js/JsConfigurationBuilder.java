package im.actor.model.js;

import im.actor.model.ConfigurationBuilder;
import im.actor.model.js.providers.*;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class JsConfigurationBuilder extends ConfigurationBuilder {
    public JsConfigurationBuilder() {
        setThreadingProvider(new JsThreadingProvider());
        setNetworkProvider(new JsNetworkProvider());
        setLog(new JsLogProvider());
        setMainThreadProvider(new JsMainThreadProvider());
        setLocale(new JsLocaleProvider());
        setCryptoProvider(new JsCryptoProvider());
        setDispatcherProvider(new JsDispatcherProvider());
        setPhoneBookProvider(new JsPhoneBookProvider());
        setStorage(new JsStorageProvider());
    }
}
