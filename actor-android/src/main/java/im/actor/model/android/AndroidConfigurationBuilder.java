package im.actor.model.android;

import android.content.Context;
import im.actor.model.jvm.JvmConfigurationBuilder;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class AndroidConfigurationBuilder extends JvmConfigurationBuilder {
    public AndroidConfigurationBuilder(Context context) {
        setMainThreadProvider(new AndroidMainThreadProvider());
        setLog(new AndroidLog());
        setPhoneBookProvider(new AndroidPhoneBook());
        setStorage(new AndroidStorageProvider());
        setNotificationProvider(new AndroidNotifications());
        setDispatcherProvider(new AndroidCallbackDispatcher());
        setFileSystemProvider(new AndroidFileProvider(context));
    }
}
