package im.actor.model.js;

import im.actor.model.ConfigurationBuilder;
import im.actor.model.js.providers.JsCryptoProvider;
import im.actor.model.js.providers.JsDispatcherProvider;
import im.actor.model.js.providers.JsLocaleProvider;
import im.actor.model.js.providers.JsLogProvider;
import im.actor.model.js.providers.JsMainThreadProvider;
import im.actor.model.js.providers.JsNetworkingProvider;
import im.actor.model.js.providers.JsNotificationsProvider;
import im.actor.model.js.providers.JsPhoneBookProvider;
import im.actor.model.js.providers.JsStorageProvider;
import im.actor.model.js.providers.JsThreadingProvider;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class JsConfigurationBuilder extends ConfigurationBuilder {
    public JsConfigurationBuilder() {
        setThreadingProvider(new JsThreadingProvider());
        setNetworkProvider(new JsNetworkingProvider());
        setLog(new JsLogProvider());
        setMainThreadProvider(new JsMainThreadProvider());
        setLocale(new JsLocaleProvider());
        setCryptoProvider(new JsCryptoProvider());
        setDispatcherProvider(new JsDispatcherProvider());
        setPhoneBookProvider(new JsPhoneBookProvider());
        setStorage(new JsStorageProvider());
        setNotificationProvider(new JsNotificationsProvider());
    }
}
