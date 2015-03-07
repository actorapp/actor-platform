package im.actor.model;

import im.actor.model.network.ConnectionEndpoint;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class Configuration {

    private final Networking networking;

    private final ConnectionEndpoint[] endpoints;

    private final Threading threading;
    private final MainThread mainThread;

    private final Storage storage;

    private final LogCallback log;

    private LocaleProvider localeProvider;

    private PhoneBookProvider phoneBookProvider;

    private CryptoProvider cryptoProvider;

    private boolean enableContactsLogging = false;
    private boolean enableNetworkLogging = false;

    private FileSystemProvider fileSystemProvider;

    private NotificationProvider notificationProvider;

    private ApiConfiguration apiConfiguration;

    public Configuration(Networking networking, ConnectionEndpoint[] endpoints,
                         Threading threading, MainThread mainThread, Storage storage,
                         LogCallback log,
                         LocaleProvider localeProvider,
                         PhoneBookProvider phoneBookProvider,
                         CryptoProvider cryptoProvider,
                         FileSystemProvider fileSystemProvider,
                         NotificationProvider notificationProvider,
                         ApiConfiguration apiConfiguration,
                         boolean enableContactsLogging,
                         boolean enableNetworkLogging) {
        this.networking = networking;
        this.endpoints = endpoints;
        this.threading = threading;
        this.mainThread = mainThread;
        this.storage = storage;
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

    public Networking getNetworking() {
        return networking;
    }

    public ConnectionEndpoint[] getEndpoints() {
        return endpoints;
    }

    public Threading getThreading() {
        return threading;
    }

    public MainThread getMainThread() {
        return mainThread;
    }

    public Storage getStorage() {
        return storage;
    }

    public LogCallback getLog() {
        return log;
    }

    public LocaleProvider getLocaleProvider() {
        return localeProvider;
    }

    public FileSystemProvider getFileSystemProvider() {
        return fileSystemProvider;
    }
}
