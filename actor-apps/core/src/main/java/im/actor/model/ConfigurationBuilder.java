/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import im.actor.model.network.ConnectionEndpoint;

/**
 * Configuration builder for starting up messenger object
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

    private HttpProvider httpProvider;

    private AnalyticsProvider analyticsProvider;

    private AppCategory appCategory = AppCategory.GENERIC;

    private DeviceCategory deviceCategory = DeviceCategory.UNKNOWN;

    private LifecycleProvider lifecycleProvider;

    private int minDelay = 100;
    private int maxDelay = 15000;
    private int maxFailureCount = 50;


    /**
     * Set App Type
     *
     * @param appCategory App Type
     * @return this
     */
    @NotNull
    @ObjectiveCName("setAppCategory:")
    public ConfigurationBuilder setAppCategory(@NotNull AppCategory appCategory) {
        this.appCategory = appCategory;
        return this;
    }

    /**
     * Setting Device Type
     *
     * @param deviceCategory Device Type
     * @return this
     */
    @NotNull
    @ObjectiveCName("setDeviceCategory:")
    public ConfigurationBuilder setDeviceCategory(@NotNull DeviceCategory deviceCategory) {
        this.deviceCategory = deviceCategory;
        return this;
    }

    @NotNull
    @ObjectiveCName("setLifecycleProvider:")
    public ConfigurationBuilder setLifecycleProvider(@NotNull LifecycleProvider lifecycleProvider) {
        this.lifecycleProvider = lifecycleProvider;
        return this;
    }

    /**
     * Set HTTP Provider
     *
     * @param httpProvider the HTTP Provider
     * @return this
     */
    @NotNull
    @ObjectiveCName("setHttpProvider:")
    public ConfigurationBuilder setHttpProvider(@NotNull HttpProvider httpProvider) {
        this.httpProvider = httpProvider;
        return this;
    }

    /**
     * Set Analytics Provider
     *
     * @param analyticsProvider the Analytics Provicer
     * @return this
     */
    @NotNull
    @ObjectiveCName("setAnalyticsProvider:")
    public ConfigurationBuilder setAnalyticsProvider(@NotNull AnalyticsProvider analyticsProvider) {
        this.analyticsProvider = analyticsProvider;
        return this;
    }

    /**
     * Set API Configuration
     *
     * @param apiConfiguration API Configuration
     * @return this
     */
    @NotNull
    @ObjectiveCName("setApiConfiguration:")
    public ConfigurationBuilder setApiConfiguration(@NotNull ApiConfiguration apiConfiguration) {
        this.apiConfiguration = apiConfiguration;
        return this;
    }

    /**
     * Set Notification provider
     *
     * @param notificationProvider Notification provider
     * @return this
     */
    @NotNull
    @ObjectiveCName("setNotificationProvider:")
    public ConfigurationBuilder setNotificationProvider(@NotNull NotificationProvider notificationProvider) {
        this.notificationProvider = notificationProvider;
        return this;
    }

    /**
     * Set File System provider
     *
     * @param fileSystemProvider File system provider
     * @return this
     */
    @NotNull
    @ObjectiveCName("setFileSystemProvider:")
    public ConfigurationBuilder setFileSystemProvider(@NotNull FileSystemProvider fileSystemProvider) {
        this.fileSystemProvider = fileSystemProvider;
        return this;
    }

    /**
     * Set Enable contacts logging
     *
     * @param enableContactsLogging Enable contacts logging flag
     * @return this
     */
    @NotNull
    @ObjectiveCName("setEnableContactsLogging:")
    public ConfigurationBuilder setEnableContactsLogging(boolean enableContactsLogging) {
        this.enableContactsLogging = enableContactsLogging;
        return this;
    }

    /**
     * Set Enable Network logging
     *
     * @param enableNetworkLogging Enable network logging
     * @return this
     */
    @NotNull
    @ObjectiveCName("setEnableNetworkLogging:")
    public ConfigurationBuilder setEnableNetworkLogging(boolean enableNetworkLogging) {
        this.enableNetworkLogging = enableNetworkLogging;
        return this;
    }

    /**
     * Set Enable file operations loggging
     *
     * @param enableFilesLogging Enable files logging
     * @return this
     */
    @NotNull
    @ObjectiveCName("setEnableFilesLogging:")
    public ConfigurationBuilder setEnableFilesLogging(boolean enableFilesLogging) {
        this.enableFilesLogging = enableFilesLogging;
        return this;
    }

    /**
     * Set Cryptography provider
     *
     * @param cryptoProvider Cryptography provider
     * @return this
     */
    @NotNull
    @ObjectiveCName("setCryptoProvider:")
    public ConfigurationBuilder setCryptoProvider(@NotNull CryptoProvider cryptoProvider) {
        this.cryptoProvider = cryptoProvider;
        return this;
    }

    /**
     * Set Phone Book provider
     *
     * @param phoneBookProvider phone book provider
     * @return this
     */
    @NotNull
    @ObjectiveCName("setPhoneBookProvider:")
    public ConfigurationBuilder setPhoneBookProvider(@NotNull PhoneBookProvider phoneBookProvider) {
        this.phoneBookProvider = phoneBookProvider;
        return this;
    }

    /**
     * Set Log provider
     *
     * @param log log provider
     * @return this
     */
    @NotNull
    @ObjectiveCName("setLogProvider:")
    public ConfigurationBuilder setLogProvider(@NotNull LogProvider log) {
        this.log = log;
        return this;
    }

    /**
     * Set Network provider
     *
     * @param networkProvider network provider
     * @return this
     */
    @NotNull
    @ObjectiveCName("setNetworkProvider:")
    public ConfigurationBuilder setNetworkProvider(@NotNull NetworkProvider networkProvider) {
        this.networkProvider = networkProvider;
        return this;
    }

    /**
     * Set Threading provider
     *
     * @param threadingProvider threading provider
     * @return this
     */
    @NotNull
    @ObjectiveCName("setThreadingProvider:")
    public ConfigurationBuilder setThreadingProvider(@NotNull ThreadingProvider threadingProvider) {
        this.threadingProvider = threadingProvider;
        return this;
    }

    /**
     * Set storage provider
     *
     * @param storageProvider Storage provider
     * @return this
     */
    @NotNull
    @ObjectiveCName("setStorageProvider:")
    public ConfigurationBuilder setStorageProvider(@NotNull StorageProvider storageProvider) {
        this.enginesFactory = storageProvider;
        return this;
    }

    /**
     * Set Locale provider
     *
     * @param localeProvider locale provider
     * @return this
     */
    @NotNull
    @ObjectiveCName("setLocaleProvider:")
    public ConfigurationBuilder setLocaleProvider(@NotNull LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
        return this;
    }

    /**
     * Set callback dispatcher provider
     *
     * @param dispatcherProvider dispatcher provider
     * @return this
     */
    @NotNull
    @ObjectiveCName("setDispatcherProvider:")
    public ConfigurationBuilder setDispatcherProvider(@NotNull DispatcherProvider dispatcherProvider) {
        this.dispatcherProvider = dispatcherProvider;
        return this;
    }

    /**
     * Set min backoff delay
     *
     * @param minDelay min connection exponential backoff delay
     * @return this
     */
    @ObjectiveCName("setMinDelay:")
    public ConfigurationBuilder setMinDelay(int minDelay) {
        this.minDelay = minDelay;
        return this;
    }

    /**
     * Set max backoff delay
     *
     * @param maxDelay max connection exponential backoff delay
     * @return this
     */
    @ObjectiveCName("setMaxDelay:")
    public ConfigurationBuilder setMaxDelay(int maxDelay) {
        this.maxDelay = maxDelay;
        return this;
    }

    /**
     * Set max connection exponential backoff failure count
     *
     * @param maxFailureCount max connection exponential backoff failure count
     * @return this
     */
    @ObjectiveCName("setMaxFailureCount:")
    public ConfigurationBuilder setMaxFailureCount(int maxFailureCount) {
        this.maxFailureCount = maxFailureCount;
        return this;
    }

    /**
     * Adding Endpoint for API
     * Valid URLs are:
     * tcp://[host]:[port]
     * tls://[host]:[port]
     * ws://[host]:[port]
     * wss://[host]:[port]
     *
     * @param url endpoint url
     * @return this
     */
    @NotNull
    @ObjectiveCName("addEndpoint:")
    public ConfigurationBuilder addEndpoint(@NotNull String url) {
        // Manual baggy parsing for GWT
        // TODO: Correct URL parsing
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

    /**
     * Setting MainThread provider
     *
     * @param mainThreadProvider main thread provider
     * @return this
     */
    @NotNull
    @ObjectiveCName("setMainThreadProvider:")
    public ConfigurationBuilder setMainThreadProvider(@NotNull MainThreadProvider mainThreadProvider) {
        this.mainThreadProvider = mainThreadProvider;
        return this;
    }

    /**
     * Build configuration
     *
     * @return result configuration
     */
    @NotNull
    @ObjectiveCName("build")
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
        if (lifecycleProvider == null) {
            throw new RuntimeException("Lifecycle Provider not set");
        }
        return new Configuration(networkProvider, endpoints.toArray(new ConnectionEndpoint[endpoints.size()]),
                threadingProvider, mainThreadProvider, enginesFactory, log, localeProvider,
                phoneBookProvider, cryptoProvider, fileSystemProvider, notificationProvider,
                dispatcherProvider, apiConfiguration, enableContactsLogging, enableNetworkLogging,
                enableFilesLogging, httpProvider, analyticsProvider, deviceCategory, appCategory,
                lifecycleProvider, minDelay, maxDelay, maxFailureCount);
    }
}
