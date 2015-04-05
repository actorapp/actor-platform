package im.actor.model;

import java.util.ArrayList;

import im.actor.model.network.ConnectionEndpoint;

/**
 * Created by ex3ndr on 16.02.15.
 */
public class ConfigurationBuilder {

    private LogProvider log;

    private NetworkProvider networkProvider;

    private ThreadingProvider threadingProvider;
    private MainThreadProvider mainThreadProvider;

    private StorageProvider enginesFactory;

    private ArrayList<ConnectionEndpoint> endpoints = new ArrayList<ConnectionEndpoint>();

    private LocaleProvider localeProvider;

    private PhoneBookProvider phoneBookProvider;

    private CryptoProvider cryptoProvider;

    private FileSystemProvider fileSystemProvider;

    private boolean enableContactsLogging = false;
    private boolean enableNetworkLogging = false;
    private boolean enableFilesLogging = false;

    private NotificationProvider notificationProvider;

    private DispatcherProvider dispatcherProvider;

    private ApiConfiguration apiConfiguration;

    public ConfigurationBuilder setApiConfiguration(ApiConfiguration apiConfiguration) {
        this.apiConfiguration = apiConfiguration;
        return this;
    }

    public ConfigurationBuilder setNotificationProvider(NotificationProvider notificationProvider) {
        this.notificationProvider = notificationProvider;
        return this;
    }

    public ConfigurationBuilder setFileSystemProvider(FileSystemProvider fileSystemProvider) {
        this.fileSystemProvider = fileSystemProvider;
        return this;
    }

    public ConfigurationBuilder setEnableContactsLogging(boolean enableContactsLogging) {
        this.enableContactsLogging = enableContactsLogging;
        return this;
    }

    public ConfigurationBuilder setEnableNetworkLogging(boolean enableNetworkLogging) {
        this.enableNetworkLogging = enableNetworkLogging;
        return this;
    }

    public ConfigurationBuilder setEnableFilesLogging(boolean enableFilesLogging) {
        this.enableFilesLogging = enableFilesLogging;
        return this;
    }

    public ConfigurationBuilder setCryptoProvider(CryptoProvider cryptoProvider) {
        this.cryptoProvider = cryptoProvider;
        return this;
    }

    public ConfigurationBuilder setPhoneBookProvider(PhoneBookProvider phoneBookProvider) {
        this.phoneBookProvider = phoneBookProvider;
        return this;
    }

    public ConfigurationBuilder setLog(LogProvider log) {
        this.log = log;
        return this;
    }

    public ConfigurationBuilder setNetworkProvider(NetworkProvider networkProvider) {
        this.networkProvider = networkProvider;
        return this;
    }

    public ConfigurationBuilder setThreadingProvider(ThreadingProvider threadingProvider) {
        this.threadingProvider = threadingProvider;
        return this;
    }

    public ConfigurationBuilder setStorage(StorageProvider storageProvider) {
        this.enginesFactory = storageProvider;
        return this;
    }

    public ConfigurationBuilder setLocale(LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
        return this;
    }

    public ConfigurationBuilder setDispatcherProvider(DispatcherProvider dispatcherProvider) {
        this.dispatcherProvider = dispatcherProvider;
        return this;
    }

    public ConfigurationBuilder addEndpoint(String url) {
        // Manual baggy parsing for GWT
        String scheme = url.substring(0, url.indexOf(":")).toLowerCase();
        String host = url.substring(url.indexOf("://") + "://".length());
        if (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        int port = -1;
        if (host.contains(":")) {
            String[] parts = host.split(":");
            host = parts[0];
            port = Integer.parseInt(parts[1]);
        }

        if (scheme.equals("ssl") || scheme.equals("tls")) {
            if (port <= 0) {
                port = 443;
            }
            endpoints.add(new ConnectionEndpoint(host, port, ConnectionEndpoint.Type.TCP_TLS));
        } else if (scheme.equals("tcp")) {
            if (port <= 0) {
                port = 80;
            }
            endpoints.add(new ConnectionEndpoint(host, port, ConnectionEndpoint.Type.TCP));
        } else if (scheme.equals("ws")) {
            if (port <= 0) {
                port = 80;
            }
            endpoints.add(new ConnectionEndpoint(host, port, ConnectionEndpoint.Type.WS));
        } else if (scheme.equals("wss")) {
            if (port <= 0) {
                port = 443;
            }
            endpoints.add(new ConnectionEndpoint(host, port, ConnectionEndpoint.Type.WS_TLS));
        } else {
            throw new RuntimeException("Unknown scheme type: " + scheme);
        }
        return this;
    }

    public ConfigurationBuilder setMainThreadProvider(MainThreadProvider mainThreadProvider) {
        this.mainThreadProvider = mainThreadProvider;
        return this;
    }

    public Configuration build() {
        if (networkProvider == null) {
            throw new RuntimeException("Networking is not set");
        }
        if (threadingProvider == null) {
            throw new RuntimeException("Threading is not set");
        }
        if (mainThreadProvider == null) {
            throw new RuntimeException("Main Thread is not set");
        }
        if (enginesFactory == null) {
            throw new RuntimeException("Storage not set");
        }
        if (endpoints.size() == 0) {
            throw new RuntimeException("Endpoints not set");
        }
        if (localeProvider == null) {
            throw new RuntimeException("Locale Provider not set");
        }
        if (phoneBookProvider == null) {
            throw new RuntimeException("Phonebook Provider not set");
        }
        if (cryptoProvider == null) {
            throw new RuntimeException("Crypto Provider not set");
        }
        if (apiConfiguration == null) {
            throw new RuntimeException("Api Configuration not set");
        }
        if (dispatcherProvider == null) {
            throw new RuntimeException("Dispatcher Provider not set");
        }
        return new Configuration(networkProvider, endpoints.toArray(new ConnectionEndpoint[endpoints.size()]),
                threadingProvider, mainThreadProvider, enginesFactory, log, localeProvider,
                phoneBookProvider, cryptoProvider, fileSystemProvider, notificationProvider,
                dispatcherProvider, apiConfiguration, enableContactsLogging, enableNetworkLogging,
                enableFilesLogging);
    }
}
