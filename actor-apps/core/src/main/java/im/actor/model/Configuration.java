/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model;

import im.actor.model.network.ConnectionEndpoint;

/**
 * Configuration for Messenger
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
    private boolean enableFilesLogging = false;

    private FileSystemProvider fileSystemProvider;

    private NotificationProvider notificationProvider;

    private ApiConfiguration apiConfiguration;

    private DispatcherProvider dispatcherProvider;

    private HttpProvider httpProvider;

    private AnalyticsProvider analyticsProvider;

    private DeviceCategory deviceCategory;

    private AppCategory appCategory;

    private LifecycleProvider lifecycleProvider;

    private int minDelay;

    private int maxDelay;

    private int maxFailureCount;

    Configuration(NetworkProvider networkProvider,
                  ConnectionEndpoint[] endpoints,
                  ThreadingProvider threadingProvider,
                  MainThreadProvider mainThreadProvider,
                  StorageProvider storageProvider,
                  LogProvider log,
                  LocaleProvider localeProvider,
                  PhoneBookProvider phoneBookProvider,
                  CryptoProvider cryptoProvider,
                  FileSystemProvider fileSystemProvider,
                  NotificationProvider notificationProvider,
                  DispatcherProvider dispatcherProvider,
                  ApiConfiguration apiConfiguration,
                  boolean enableContactsLogging,
                  boolean enableNetworkLogging,
                  boolean enableFilesLogging,
                  HttpProvider httpProvider,
                  AnalyticsProvider analyticsProvider,
                  DeviceCategory deviceCategory,
                  AppCategory appCategory,
                  LifecycleProvider lifecycleProvider,
                  int minDelay,
                  int maxDelay,
                  int maxFailureCount) {
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
        this.enableFilesLogging = enableFilesLogging;
        this.notificationProvider = notificationProvider;
        this.apiConfiguration = apiConfiguration;
        this.dispatcherProvider = dispatcherProvider;
        this.httpProvider = httpProvider;
        this.analyticsProvider = analyticsProvider;
        this.deviceCategory = deviceCategory;
        this.appCategory = appCategory;
        this.lifecycleProvider = lifecycleProvider;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.maxFailureCount = maxFailureCount;
    }

    /**
     * Get Device Type
     *
     * @return Device Type
     */
    public DeviceCategory getDeviceCategory() {
        return deviceCategory;
    }

    /**
     * Get App Type
     *
     * @return App Type
     */
    public AppCategory getAppCategory() {
        return appCategory;
    }

    /**
     * Get API Configuration
     *
     * @return API Configuration
     */
    public ApiConfiguration getApiConfiguration() {
        return apiConfiguration;
    }

    /**
     * Get Notification provider
     *
     * @return notification provider
     */
    public NotificationProvider getNotificationProvider() {
        return notificationProvider;
    }

    /**
     * Get Enable contacts logging flag
     *
     * @return is enable contacts logging
     */
    public boolean isEnableContactsLogging() {
        return enableContactsLogging;
    }

    /**
     * Get Enable network logging flag
     *
     * @return is enable network logging
     */
    public boolean isEnableNetworkLogging() {
        return enableNetworkLogging;
    }

    /**
     * Get Enable files logging flag
     *
     * @return is enable files logging
     */
    public boolean isEnableFilesLogging() {
        return enableFilesLogging;
    }

    /**
     * Get Crypto provider
     *
     * @return Crypto Provider
     */
    public CryptoProvider getCryptoProvider() {
        return cryptoProvider;
    }

    /**
     * Get PhoneBook provider
     *
     * @return PhoneBook provider
     */
    public PhoneBookProvider getPhoneBookProvider() {
        return phoneBookProvider;
    }

    /**
     * Get Network provider
     *
     * @return Network provider
     */
    public NetworkProvider getNetworkProvider() {
        return networkProvider;
    }

    /**
     * Get Endpoints
     *
     * @return Endpoints
     */
    public ConnectionEndpoint[] getEndpoints() {
        return endpoints;
    }

    /**
     * Get Threading provider
     *
     * @return Threading provider
     */
    public ThreadingProvider getThreadingProvider() {
        return threadingProvider;
    }

    /**
     * Get Main Thread provider
     *
     * @return Main Thread provider
     */
    public MainThreadProvider getMainThreadProvider() {
        return mainThreadProvider;
    }

    /**
     * Get Storage provider
     *
     * @return Storage provider
     */
    public StorageProvider getStorageProvider() {
        return storageProvider;
    }

    /**
     * Get Log provider
     *
     * @return Log provider
     */
    public LogProvider getLog() {
        return log;
    }

    /**
     * Get Locale provider
     *
     * @return Locale provider
     */
    public LocaleProvider getLocaleProvider() {
        return localeProvider;
    }

    /**
     * Get FileSystem provider
     *
     * @return FileSystem provider
     */
    public FileSystemProvider getFileSystemProvider() {
        return fileSystemProvider;
    }

    /**
     * Get Callback Dispatcher provider
     *
     * @return Callback Dispatcher provider
     */
    public DispatcherProvider getDispatcherProvider() {
        return dispatcherProvider;
    }

    /**
     * Get HTTP support provider
     *
     * @return HTTP support provider
     */
    public HttpProvider getHttpProvider() {
        return httpProvider;
    }

    /**
     * Get Analytics provider
     *
     * @return Analytics provider
     */
    public AnalyticsProvider getAnalyticsProvider() {
        return analyticsProvider;
    }

    /**
     * Get Application Lifecycle provider
     *
     * @return Lifecycle provider
     */
    public LifecycleProvider getLifecycleProvider() {
        return lifecycleProvider;
    }

    /**
     * Get Application min connection exponential backoff delay
     *
     * @return min connection exponential backoff delay
     */
    public int getMinDelay() {
        return minDelay;
    }

    /**
     * Get Application max connection exponential backoff delay
     *
     * @return max connection exponential backoff delay
     */
    public int getMaxDelay() {
        return maxDelay;
    }

    /**
     * Get Application max connection exponential backoff failure count
     *
     * @return max connection exponential backoff failure count
     */
    public int getMaxFailureCount() {
        return maxFailureCount;
    }
}
