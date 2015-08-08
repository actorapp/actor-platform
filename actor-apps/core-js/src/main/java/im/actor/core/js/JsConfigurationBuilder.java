/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js;

import im.actor.core.AppCategory;
import im.actor.core.ConfigurationBuilder;
import im.actor.core.DeviceCategory;
import im.actor.core.js.providers.JsCryptoProvider;
import im.actor.core.js.providers.JsDispatcherProvider;
import im.actor.core.js.providers.JsHttpProvider;
import im.actor.core.js.providers.JsLifecycleProvider;
import im.actor.core.js.providers.JsLocaleProvider;
import im.actor.core.js.providers.JsLogProvider;
import im.actor.core.js.providers.JsMainThreadProvider;
import im.actor.core.js.providers.JsNetworkingProvider;
import im.actor.core.js.providers.JsNotificationsProvider;
import im.actor.core.js.providers.JsPhoneBookProvider;
import im.actor.core.js.providers.JsStorageProvider;
import im.actor.core.js.providers.JsThreadingProvider;

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
