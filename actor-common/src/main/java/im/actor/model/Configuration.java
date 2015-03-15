package im.actor.model;

import im.actor.model.network.ConnectionEndpoint;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class Configuration {

    private final NetworkProvider networkProvider;

    private final ConnectionEndpoint[] endpoints;

    private final ThreadingProvider threadingProvider;
    private final MainThreadProvider mainThreadProvider;

    private final StorageProvider storageProvider;

    private final LogProvider log;

    private LocaleProvider localeProvider;

    private PhoneBookProvider phoneBookProvider;

    private CryptoProvider cryptoProvider;

    private boolean enableContactsLogging = false;
    private boolean enableNetworkLogging = false;

    private FileSystemProvider fileSystemProvider;

    private NotificationProvider notificationProvider;

    private ApiConfiguration apiConfiguration;

    public Configuration(NetworkProvider networkProvider, ConnectionEndpoint[] endpoints,
                         ThreadingProvider threadingProvider, MainThreadProvider mainThreadProvider, StorageProvider storageProvider,
                         LogProvider log,
                         LocaleProvider localeProvider,
                         PhoneBookProvider phoneBookProvider,
                         CryptoProvider cryptoProvider,
                         FileSystemProvider fileSystemProvider,
                         NotificationProvider notificationProvider,
                         ApiConfiguration apiConfiguration,
                         boolean enableContactsLogging,
                         boolean enableNetworkLogging) {
        this.networkProvider = networkProvider;
        this.endpoints = endpoints;
        this.threadingProvider = threadingProvider;
        this.mainThreadProvider = mainThreadProvider;
        this.storageProvider = storageProvider;
        this.log = log;
        this.localeProvider = localeProvider;
        this.phoneBookProvider = phoneBookProvider;
        this.cryptoProvider = cryptoProvider;
        this.fileSystemProvider = fileSystemProvider;
        this.enableContactsLogging = enableContactsLogging;
        this.enableNetworkLogging = enableNetworkLogging;
        this.notificationProvider = notificationProvider;
        this.apiConfiguration = apiConfiguration;
    }

    public ApiConfiguration getApiConfiguration() {
        return apiConfiguration;
    }

    public NotificationProvider getNotificationProvider() {
        return notificationProvider;
    }

    public boolean isEnableContactsLogging() {
        return enableContactsLogging;
    }

    public boolean isEnableNetworkLogging() {
        return enableNetworkLogging;
    }

    public CryptoProvider getCryptoProvider() {
        return cryptoProvider;
    }

    public PhoneBookProvider getPhoneBookProvider() {
        return phoneBookProvider;
    }

    public NetworkProvider getNetworkProvider() {
        return networkProvider;
    }

    public ConnectionEndpoint[] getEndpoints() {
        return endpoints;
    }

    public ThreadingProvider getThreadingProvider() {
        return threadingProvider;
    }

    public MainThreadProvider getMainThreadProvider() {
        return mainThreadProvider;
    }

    public StorageProvider getStorageProvider() {
        return storageProvider;
    }

    public LogProvider getLog() {
        return log;
    }

    public LocaleProvider getLocaleProvider() {
        return localeProvider;
    }

    public FileSystemProvider getFileSystemProvider() {
        return fileSystemProvider;
    }
}
