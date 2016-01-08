/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core;

import im.actor.core.network.TrustedKey;
import im.actor.runtime.mtproto.ConnectionEndpoint;

/**
 * Configuration for Messenger
 */
public class Configuration {

    private final ConfigurationExtension[] extensions;

    private final ConnectionEndpoint[] endpoints;

    private final TrustedKey[] trustedKeys;

    private PhoneBookProvider phoneBookProvider;

    private boolean enableContactsLogging = false;
    private boolean enableNetworkLogging = false;
    private boolean enableFilesLogging = false;

    private NotificationProvider notificationProvider;

    private ApiConfiguration apiConfiguration;

    private DeviceCategory deviceCategory;

    private PlatformType platformType;

    private String timeZone;
    private String[] preferredLanguages;

    private int minDelay;

    private int maxDelay;

    private int maxFailureCount;

    private String customAppName;

    Configuration(ConnectionEndpoint[] endpoints,
                  PhoneBookProvider phoneBookProvider,
                  NotificationProvider notificationProvider,
                  ApiConfiguration apiConfiguration,
                  boolean enableContactsLogging,
                  boolean enableNetworkLogging,
                  boolean enableFilesLogging,
                  DeviceCategory deviceCategory,
                  PlatformType platformType,
                  int minDelay,
                  int maxDelay,
                  int maxFailureCount,
                  ConfigurationExtension[] extensions,
                  String timeZone,
                  String[] preferredLanguages,
                  String customAppName,
                  TrustedKey[] trustedKeys) {
        this.endpoints = endpoints;
        this.phoneBookProvider = phoneBookProvider;
        this.enableContactsLogging = enableContactsLogging;
        this.enableNetworkLogging = enableNetworkLogging;
        this.enableFilesLogging = enableFilesLogging;
        this.notificationProvider = notificationProvider;
        this.apiConfiguration = apiConfiguration;
        this.deviceCategory = deviceCategory;
        this.platformType = platformType;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.maxFailureCount = maxFailureCount;
        this.extensions = extensions;
        this.timeZone = timeZone;
        this.preferredLanguages = preferredLanguages;
        this.customAppName = customAppName;
        this.trustedKeys = trustedKeys;
    }

    /**
     * Getting Trusted keys
     *
     * @return trusted keys if set
     */
    public TrustedKey[] getTrustedKeys() {
        return trustedKeys;
    }

    /**
     * Get Custom Application name
     *
     * @return Application Name if set, otherwise is null
     */
    public String getCustomAppName() {
        return customAppName;
    }

    /**
     * Get configured extensions
     *
     * @return extensions
     */
    public ConfigurationExtension[] getExtensions() {
        return extensions;
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
     * Get Platform Type
     *
     * @return App Type
     */
    public PlatformType getPlatformType() {
        return platformType;
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
     * Get PhoneBook provider
     *
     * @return PhoneBook provider
     */
    public PhoneBookProvider getPhoneBookProvider() {
        return phoneBookProvider;
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

    /**
     * Get device time zone
     *
     * @return device timezone in Tz-format
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * Get preferred languages
     *
     * @return preferred languages
     */
    public String[] getPreferredLanguages() {
        return preferredLanguages;
    }
}
