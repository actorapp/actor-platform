package im.actor.model.js;

import im.actor.model.ConfigurationBuilder;
import im.actor.model.js.providers.*;
import im.actor.model.js.providers.websocket.PlatformNetworkProvider;
import im.actor.model.js.providers.websocket.WebSocketAsyncConnectionFactory;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class JsConfigurationBuilder extends ConfigurationBuilder {
    public JsConfigurationBuilder() {
        setThreadingProvider(new JsThreadingProvider());
        setNetworkProvider(new PlatformNetworkProvider(new WebSocketAsyncConnectionFactory()));
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
