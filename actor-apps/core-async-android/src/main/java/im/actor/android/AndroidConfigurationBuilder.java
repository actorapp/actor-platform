/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.android;

import android.content.Context;

import im.actor.model.AsyncConfigurationBuilder;

public class AndroidConfigurationBuilder extends AsyncConfigurationBuilder {

    public AndroidConfigurationBuilder(String locale, Context context) {
        setMainThreadProvider(new AndroidMainThreadProvider());
        setThreadingProvider(new AndroidThreadingProvider("europe.pool.ntp.org", context));
        setLogProvider(new AndroidLog());
        setStorageProvider(new AndroidStorageProvider(context));
        setDispatcherProvider(new AndroidCallbackDispatcher());
        setFileSystemProvider(new AndroidFileProvider(context));
        setCryptoProvider(new AndroidCryptoProvider());
        setHttpProvider(new AndroidHttpSupport());
        setLifecycleProvider(new AndroidLifecycleProvider());
        setNetworkProvider(new AndroidNetworkProvider());
        setLocaleProvider(new AndroidLocale(locale));
    }
}
