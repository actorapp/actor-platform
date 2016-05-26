/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core;

import com.google.j2objc.annotations.Property;

import im.actor.core.network.TrustedKey;
import im.actor.core.providers.NotificationProvider;
import im.actor.core.providers.PhoneBookProvider;
import im.actor.core.providers.CallsProvider;
import im.actor.runtime.mtproto.ConnectionEndpoint;
import im.actor.runtime.webrtc.WebRTCIceServer;

/**
 * Configuration for Messenger
 */
public class Configuration {

    @Property("readonly, nonatomic")
    private final ConnectionEndpoint[] endpoints;
    @Property("readonly, nonatomic")
    private final TrustedKey[] trustedKeys;
    @Property("readonly, nonatomic")
    private final PhoneBookProvider phoneBookProvider;
    @Property("readonly, nonatomic")
    private final boolean voiceCallsEnabled;
    @Property("readonly, nonatomic")
    private final boolean videoCallsEnabled;
    @Property("readonly, nonatomic")
    private final boolean enableContactsLogging;
    @Property("readonly, nonatomic")
    private final boolean enableNetworkLogging;
    @Property("readonly, nonatomic")
    private final boolean enableFilesLogging;
    @Property("readonly, nonatomic")
    private final NotificationProvider notificationProvider;
    @Property("readonly, nonatomic")
    private final ApiConfiguration apiConfiguration;
    @Property("readonly, nonatomic")
    private final DeviceCategory deviceCategory;
    @Property("readonly, nonatomic")
    private final PlatformType platformType;
    @Property("readonly, nonatomic")
    private final String timeZone;
    @Property("readonly, nonatomic")
    private final String[] preferredLanguages;
    @Property("readonly, nonatomic")
    private final int minDelay;
    @Property("readonly, nonatomic")
    private final int maxDelay;
    @Property("readonly, nonatomic")
    private final int maxFailureCount;
    @Property("readonly, nonatomic")
    private final String customAppName;
    @Property("readonly, nonatomic")
    private final boolean enablePhoneBookImport;
    @Property("readonly, nonatomic")
    private final CallsProvider callsProvider;
    @Property("readonly, nonatomic")
    private final boolean isEnabledGroupedChatList;

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
                  String timeZone,
                  String[] preferredLanguages,
                  String customAppName,
                  TrustedKey[] trustedKeys,
                  boolean enablePhoneBookImport,
                  CallsProvider callsProvider,
                  boolean voiceCallsEnabled,
                  boolean videoCallsEnabled,
                  boolean isEnabledGroupedChatList) {
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
        this.timeZone = timeZone;
        this.preferredLanguages = preferredLanguages;
        this.customAppName = customAppName;
        this.trustedKeys = trustedKeys;
        this.enablePhoneBookImport = enablePhoneBookImport;
        this.callsProvider = callsProvider;
        this.voiceCallsEnabled = voiceCallsEnabled;
        this.videoCallsEnabled = videoCallsEnabled;
        this.isEnabledGroupedChatList = isEnabledGroupedChatList;
    }

    /**
     * Getting If Voice Calls Enabled
     *
     * @return voice calls enabled
     */
    public boolean isVoiceCallsEnabled() {
        return voiceCallsEnabled;
    }

    /**
     * Getting If Video Calls Enabled
     *
     * @return video calls enabled
     */
    public boolean isVideoCallsEnabled() {
        return videoCallsEnabled;
    }

    /**
     * Getting Calls provider if set
     *
     * @return Calls provider
     */
    public CallsProvider getCallsProvider() {
        return callsProvider;
    }

    /**
     * Getting if app automatically imports phone book to server
     *
     * @return if phone book enabled
     */
    public boolean isEnablePhoneBookImport() {
        return enablePhoneBookImport;
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

    /**
     * Get If Grouped Chat List enabled
     *
     * @return is grouped chat list enabled
     */
    public boolean isEnabledGroupedChatList() {
        return isEnabledGroupedChatList;
    }
}
