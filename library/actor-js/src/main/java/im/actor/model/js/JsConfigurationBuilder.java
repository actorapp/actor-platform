/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js;

import im.actor.model.AppCategory;
import im.actor.model.ConfigurationBuilder;
import im.actor.model.DeviceCategory;
import im.actor.model.js.providers.JsCryptoProvider;
import im.actor.model.js.providers.JsDispatcherProvider;
import im.actor.model.js.providers.JsHttpProvider;
import im.actor.model.js.providers.JsLifecycleProvider;
import im.actor.model.js.providers.JsLocaleProvider;
import im.actor.model.js.providers.JsLogProvider;
import im.actor.model.js.providers.JsMainThreadProvider;
import im.actor.model.js.providers.JsNetworkingProvider;
import im.actor.model.js.providers.JsNotificationsProvider;
import im.actor.model.js.providers.JsPhoneBookProvider;
import im.actor.model.js.providers.JsStorageProvider;
import im.actor.model.js.providers.JsThreadingProvider;

public class JsConfigurationBuilder extends ConfigurationBuilder {
    public JsConfigurationBuilder() {
        setThreadingProvider(new JsThreadingProvider());
        setNetworkProvider(new JsNetworkingProvider());
        setLogProvider(new JsLogProvider());
        setMainThreadProvider(new JsMainThreadProvider());
        setLocaleProvider(new JsLocaleProvider());
        setCryptoProvider(new JsCryptoProvider());
        setDispatcherProvider(new JsDispatcherProvider());
        setPhoneBookProvider(new JsPhoneBookProvider());
        setStorageProvider(new JsStorageProvider());
        setNotificationProvider(new JsNotificationsProvider());
        setHttpProvider(new JsHttpProvider());
        setAppCategory(AppCategory.WEB);
        setDeviceCategory(DeviceCategory.DESKTOP);
        setLifecycleProvider(new JsLifecycleProvider());
    }
}
