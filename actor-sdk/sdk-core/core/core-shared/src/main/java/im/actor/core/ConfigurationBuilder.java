/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import im.actor.core.network.TrustedKey;
import im.actor.core.providers.NotificationProvider;
import im.actor.core.providers.PhoneBookProvider;
import im.actor.core.providers.CallsProvider;
import im.actor.core.util.StringMatch;
import im.actor.runtime.Crypto;
import im.actor.runtime.Log;
import im.actor.runtime.mtproto.ConnectionEndpoint;
import im.actor.runtime.mtproto.ConnectionEndpointArray;
import im.actor.runtime.util.Hex;
import im.actor.runtime.webrtc.WebRTCIceServer;

/**
 * Configuration builder for starting up messenger object
 */
public class ConfigurationBuilder {

    private ArrayList<TrustedKey> trustedKeys = new ArrayList<>();
    private ConnectionEndpointArray endpoints = new ConnectionEndpointArray();

    private PhoneBookProvider phoneBookProvider;

    private boolean voiceCallsEnabled = true;
    private boolean videoCallsEnabled = false;

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

    private String timeZone;
    private ArrayList<String> preferredLanguages = new ArrayList<>();

    private String customAppName;

    private boolean isPhoneBookImportEnabled = true;
    private boolean isOnClientPrivacyEnabled = false;

    private CallsProvider callsProvider;
    private RawUpdatesHandler rawUpdatesHandler;

    private boolean isEnabledGroupedChatList = true;

    private ArrayList<String> autoJoinGroups = new ArrayList<>();
    private AutoJoinType autoJoinType = AutoJoinType.AFTER_INIT;

    /**
     * Setting Auto Join to group type: when to join to your groups
     *
     * @param autoJoinType auto join type
     * @return this
     */
    @ObjectiveCName("setAutoJoinType:")
    public ConfigurationBuilder setAutoJoinType(AutoJoinType autoJoinType) {
        this.autoJoinType = autoJoinType;
        return this;
    }


    /**
     * Setting if grouped chat list support enabled
     *
     * @param isEnabledGroupedChatList if grouped chat list enabled
     * @return this
     */
    @ObjectiveCName("setIsEnabledGroupedChatList:")
    public ConfigurationBuilder setIsEnabledGroupedChatList(boolean isEnabledGroupedChatList) {
        this.isEnabledGroupedChatList = isEnabledGroupedChatList;
        return this;
    }

    /**
     * Setting If Voice Calls enabled in App. By default is True.
     *
     * @param voiceCallsEnabled if voice calls enabled
     * @return this
     */
    @ObjectiveCName("setVoiceCallsEnabled:")
    public ConfigurationBuilder setVoiceCallsEnabled(boolean voiceCallsEnabled) {
        this.voiceCallsEnabled = voiceCallsEnabled;
        return this;
    }

    /**
     * Setting If Video Calls enabled in App. By default is False.
     *
     * @param videoCallsEnabled if voice calls enabled
     * @return this
     */
    @ObjectiveCName("setVideoCallsEnabled:")
    public ConfigurationBuilder setVideoCallsEnabled(boolean videoCallsEnabled) {
        this.videoCallsEnabled = videoCallsEnabled;
        return this;
    }

    /**
     * Setting if application need to upload phone book to server
     *
     * @param isPhoneBookImportEnabled enabled flag
     * @return this
     */
    @NotNull
    @ObjectiveCName("setPhoneBookImportEnabled:")
    public ConfigurationBuilder setPhoneBookImportEnabled(boolean isPhoneBookImportEnabled) {
        this.isPhoneBookImportEnabled = isPhoneBookImportEnabled;
        return this;
    }

    /**
     * Setting if application uses on client contacts privacy
     *
     * @param isOnClientPrivacyEnabled enabled flag
     * @return this
     */
    @NotNull
    @ObjectiveCName("setOnClientPrivacyEnabled:")
    public ConfigurationBuilder setOnClientPrivacyEnabled(boolean isOnClientPrivacyEnabled) {
        this.isOnClientPrivacyEnabled = isOnClientPrivacyEnabled;
        return this;
    }

    /**
     * Setting Web RTC support provider
     *
     * @param callsProvider WebRTC provider
     * @return this
     */
    @NotNull
    @ObjectiveCName("setCallsProvider:")
    public ConfigurationBuilder setCallsProvider(CallsProvider callsProvider) {
        this.callsProvider = callsProvider;
        return this;
    }

    /**
     * Setting raw updates handler
     *
     * @param rawUpdatesHandler raw updates handler
     * @return this
     */
    @NotNull
    @ObjectiveCName("setRawUpdatesHandler:")
    public ConfigurationBuilder setRawUpdatesHandler(RawUpdatesHandler rawUpdatesHandler) {
        this.rawUpdatesHandler = rawUpdatesHandler;
        return this;
    }

    /**
     * Adding Trusted key for protocol encryption securing
     *
     * @param trustedKey hex representation of trusted key
     * @return this
     */
    @NotNull
    @ObjectiveCName("addTrustedKey:")
    public ConfigurationBuilder addTrustedKey(String trustedKey) {
        trustedKeys.add(new TrustedKey(trustedKey));
        return this;
    }

    /**
     * Adding group to auto join of users
     *
     * @param groupTokenOrShortName group's token or short name
     * @return this
     */
    @NotNull
    @ObjectiveCName("addAutoJoinGroupWithToken:")
    public ConfigurationBuilder addAutoJoinGroup(String groupTokenOrShortName) {
        autoJoinGroups.add(groupTokenOrShortName);
        return this;
    }

    /**
     * Setting custom application name
     *
     * @param customAppName Name of your App
     * @return this
     */
    @NotNull
    @ObjectiveCName("setCustomAppName:")
    public ConfigurationBuilder setCustomAppName(String customAppName) {
        this.customAppName = customAppName;
        return this;
    }

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
     * Setting Device TimeZone
     *
     * @param timeZone device time zone
     * @return this
     */
    @NotNull
    @ObjectiveCName("setTimeZone:")
    public ConfigurationBuilder setTimeZone(String timeZone) {
        this.timeZone = timeZone;
        return this;
    }

    /**
     * Adding preferred language
     *
     * @param language language code
     * @return this
     */
    @NotNull
    @ObjectiveCName("addPreferredLanguage:")
    public ConfigurationBuilder addPreferredLanguage(String language) {
        if (!preferredLanguages.contains(language)) {
            preferredLanguages.add(language);
        }
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
        try {
            endpoints.addEndpoint(url);
        } catch (ConnectionEndpointArray.UnknownSchemeException e) {
            throw new RuntimeException(e.getMessage());
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
        if (trustedKeys.size() == 0) {
            Log.w("ConfigurationBuilder", "No Trusted keys set. Using anonymous server authentication.");
        }
        return new Configuration(endpoints.toArray(new ConnectionEndpoint[endpoints.size()]),
                phoneBookProvider, notificationProvider,
                apiConfiguration, enableContactsLogging, enableNetworkLogging,
                enableFilesLogging, deviceCategory, platformType,
                minDelay, maxDelay, maxFailureCount,
                timeZone, preferredLanguages.toArray(new String[preferredLanguages.size()]),
                customAppName,
                trustedKeys.toArray(new TrustedKey[trustedKeys.size()]),
                isPhoneBookImportEnabled,
                isOnClientPrivacyEnabled,
                callsProvider,
                rawUpdatesHandler,
                voiceCallsEnabled,
                videoCallsEnabled,
                isEnabledGroupedChatList,
                autoJoinGroups.toArray(new String[autoJoinGroups.size()]),
                autoJoinType);
    }
}
