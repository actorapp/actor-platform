/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import im.actor.runtime.mtproto.ConnectionEndpoint;

/**
 * Configuration builder for starting up messenger object
 */
public class ConfigurationBuilder {

    private ArrayList<ConnectionEndpoint> endpoints = new ArrayList<ConnectionEndpoint>();

    private PhoneBookProvider phoneBookProvider;

    private boolean enableContactsLogging = false;
    private boolean enableNetworkLogging = false;
    private boolean enableFilesLogging = false;

    private NotificationProvider notificationProvider;

    private ApiConfiguration apiConfiguration;

    private PlatformType platformType = PlatformType.GENERIC;

    private DeviceCategory deviceCategory = DeviceCategory.UNKNOWN;

    private int minDelay = 100;
    private int maxDelay = 15000;
    private int maxFailureCount = 50;

    /**
     * Set App Type
     *
     * @param platformType App Type
     * @return this
     */
    @NotNull
    @ObjectiveCName("setPlatformType:")
    public ConfigurationBuilder setPlatformType(@NotNull PlatformType platformType) {
        this.platformType = platformType;
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
     * Build configuration
     *
     * @return result configuration
     */
    @NotNull
    @ObjectiveCName("build")
    public Configuration build() {
        if (endpoints.size() == 0) {
            throw new RuntimeException("Endpoints not set");
        }
        if (phoneBookProvider == null) {
            throw new RuntimeException("Phonebook Provider not set");
        }
        if (apiConfiguration == null) {
            throw new RuntimeException("Api Configuration not set");
        }
        if (deviceCategory == null) {
            throw new RuntimeException("Device Category not set");
        }
        if (platformType == null) {
            throw new RuntimeException("App Category not set");
        }
        return new Configuration(endpoints.toArray(new ConnectionEndpoint[endpoints.size()]),
                phoneBookProvider, notificationProvider,
                apiConfiguration, enableContactsLogging, enableNetworkLogging,
                enableFilesLogging, deviceCategory, platformType,
                minDelay, maxDelay, maxFailureCount);
    }
}
