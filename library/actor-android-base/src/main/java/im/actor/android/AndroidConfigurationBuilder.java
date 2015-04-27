package im.actor.android;

import android.content.Context;

import im.actor.model.jvm.JvmConfigurationBuilder;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class AndroidConfigurationBuilder extends JvmConfigurationBuilder {

    public AndroidConfigurationBuilder(String locale, Context context) {
        super(locale);
        setMainThreadProvider(new AndroidMainThreadProvider());
        setThreadingProvider(new AndroidThreadingProvider("europe.pool.ntp.org", context));
        setLog(new AndroidLog());
        setStorage(new AndroidStorageProvider(context));
        setDispatcherProvider(new AndroidCallbackDispatcher());
        setFileSystemProvider(new AndroidFileProvider(context));
        setCryptoProvider(new AndroidCryptoProvider());
        setHttpDownloaderProvider(new AndroidHttpSupport());
    }
}
