package im.actor.sdk;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.TimeZone;

import im.actor.core.AndroidMessenger;
import im.actor.core.ApiConfiguration;
import im.actor.core.AutoJoinType;
import im.actor.core.ConfigurationBuilder;
import im.actor.core.DeviceCategory;
import im.actor.core.PlatformType;
import im.actor.core.entity.Peer;
import im.actor.runtime.Log;
import im.actor.runtime.Runtime;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.threading.ThreadDispatcher;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.root.RootActivity;
import im.actor.sdk.controllers.conversation.ChatActivity;
import im.actor.sdk.controllers.auth.AuthActivity;
import im.actor.sdk.controllers.group.GroupInfoActivity;
import im.actor.sdk.controllers.settings.MyProfileActivity;
import im.actor.sdk.controllers.settings.SecuritySettingsActivity;
import im.actor.sdk.core.AndroidCallProvider;
import im.actor.sdk.core.AndroidNotifications;
import im.actor.sdk.core.AndroidPhoneBook;
import im.actor.sdk.core.ActorPushManager;
import im.actor.sdk.intents.ActorIntent;
import im.actor.sdk.intents.ActorIntentActivity;
import im.actor.sdk.intents.ActorIntentFragmentActivity;
import im.actor.sdk.push.ActorPushRegister;
import im.actor.sdk.services.KeepAliveService;
import im.actor.sdk.util.Devices;
import im.actor.sdk.view.emoji.SmileProcessor;
import im.actor.runtime.android.AndroidContext;


public class ActorSDK {

    private static final String TAG = "ActorSDK";

    private static volatile ActorSDK sdk = new ActorSDK();
    /**
     * ActorStyle style can be used for configuring application appearance, for example - colors, backgrounds etc.
     */
    public ActorStyle style = new ActorStyle();


    //
    // SDK Objects
    //
    /**
     * Application Context
     */
    private Application application;
    /**
     * Actor Messenger instance
     */
    private AndroidMessenger messenger;

    /**
     * Is Messenger Loaded
     */
    private boolean isLoaded;

    /**
     * Loading Lock
     */
    private final Object LOAD_LOCK = new Object();


    //
    // SDK Config
    //
    /**
     * Server Endpoints
     */
    private String[] endpoints = new String[0];

    /**
     * Trusted Encryption keys
     */
    private String[] trustedKeys = new String[0];

    /**
     * API App Id
     */
    private int apiAppId = 1;
    /**
     * API App Key
     */
    private String apiAppKey = "4295f9666fad3faf2d04277fe7a0c40ff39a85d313de5348ad8ffa650ad71855";
    /**
     * Actor App Name
     */
    private String appName = "Actor";
    /**
     * Push Registration Id
     */
    private long pushId = 0;
    /**
     * Actor Push Endpoint
     */
    private String actorPushEndpoint = "https://push.actor.im/apps/31337/subscriptions";
    /**
     * Is Keeping app alive enabled
     */
    private boolean isKeepAliveEnabled = false;

    /**
     * Custom application name
     */
    private String customApplicationName = null;
    /**
     * Invite url
     */
    private String inviteUrl = "https://actor.im/dl";
    /**
     * Invite url
     */
    @Nullable
    private String groupInvitePrefix = "actor.im/join/";
    /**
     * Help phone
     */
    private String helpPhone = "75551234567";
    /**
     * Home page
     */
    private String homePage = "https://actor.im";
    /**
     * Twitter
     */
    private String twitter = "actorapp";

    /**
     * Terms of service
     */
    private String tosUrl = null;

    private String tosText = null;

    /**
     * Privacy policy
     */
    private String privacyUrl = null;

    private String privacyText = null;

    /**
     * Fast share menu is experimental feature - disabled be default
     */
    private boolean fastShareEnabled = false;

    /**
     * Auto Join Groups
     */
    private String[] autoJoinGroups = new String[0];
    private AutoJoinType autoJoinType = AutoJoinType.AFTER_INIT;

    /**
     * Auth type - binary mask for auth type
     */
    private int authType = AuthActivity.AUTH_TYPE_PHONE + AuthActivity.AUTH_TYPE_EMAIL;

    /**
     * Alternate endpoints - allow choose alternate endpoint on auth - disabled be default
     */
    private boolean useAlternateEndpoints = false;

    /**
     * Delegate
     */
    @NotNull
    private ActorSDKDelegate delegate = new BaseActorSDKDelegate();

    /**
     * Call enabled
     */
    private boolean callsEnabled = false;
    private boolean videoCallsEnabled = false;

    private boolean onClientPrivacyEnabled = false;

    private String inviteDataUrl = "https://api.actor.im/v1/groups/invites/";

    private ActorSDK() {
        endpoints = new String[]{
                "tcp://front1-mtproto-api-rev3.actor.im:443",
                "tcp://front2-mtproto-api-rev3.actor.im:443",
                "tcp://front3-mtproto-api-rev3.actor.im:443"
        };
        trustedKeys = new String[]{
                "d9d34ed487bd5b434eda2ef2c283db587c3ae7fb88405c3834d9d1a6d247145b",
                "4bd5422b50c585b5c8575d085e9fae01c126baa968dab56a396156759d5a7b46",
                "ff61103913aed3a9a689b6d77473bc428d363a3421fdd48a8e307a08e404f02c",
                "20613ab577f0891102b1f0a400ca53149e2dd05da0b77a728b62f5ebc8095878",
                "fc49f2f2465f5b4e038ec7c070975858a8b5542aa6ec1f927a57c4f646e1c143",
                "6709b8b733a9f20a96b9091767ac19fd6a2a978ba0dccc85a9ac8f6b6560ac1a"
        };
    }

    /**
     * Shared ActorSDK. Use this method to get instance of SDK for configuration and starting up
     *
     * @return ActorSDK instance.
     */
    public static ActorSDK sharedActor() {
        // Use function if we will replace implementation for some cases
        return sdk;
    }

    //
    // SDK Initialization
    //

    public void createActor(final Application application) {

        this.application = application;

        ThreadDispatcher.pushDispatcher(Runtime::postToMainThread);

        Runtime.dispatch(() -> {

            //
            // SDK Tools
            //
            ImagePipelineConfig config = ImagePipelineConfig.newBuilder(application)
                    .setDownsampleEnabled(true)
                    .build();
            Fresco.initialize(application, config);

            SmileProcessor emojiProcessor = new SmileProcessor(application);
            ActorSystem.system().addDispatcher("voice_capture_dispatcher", 1);

            //
            // SDK Configuration
            //
            ConfigurationBuilder builder = new ConfigurationBuilder();
            for (String s : endpoints) {
                builder.addEndpoint(s);
            }
            for (String t : trustedKeys) {
                builder.addTrustedKey(t);
            }
            builder.setPhoneBookProvider(new AndroidPhoneBook());
            builder.setVideoCallsEnabled(videoCallsEnabled);
            builder.setOnClientPrivacyEnabled(onClientPrivacyEnabled);
            builder.setNotificationProvider(new AndroidNotifications(application));
            builder.setDeviceCategory(DeviceCategory.MOBILE);
            builder.setPlatformType(PlatformType.ANDROID);
            builder.setIsEnabledGroupedChatList(false);
            builder.setApiConfiguration(new ApiConfiguration(
                    appName,
                    apiAppId,
                    apiAppKey,
                    Devices.getDeviceName(),
                    AndroidContext.getContext().getPackageName() + ":" + Build.SERIAL));

            //
            // Adding Locales
            //
            Locale defaultLocale = Locale.getDefault();
            Log.d(TAG, "Found Locale: " + defaultLocale.getLanguage() + "-" + defaultLocale.getCountry());
            Log.d(TAG, "Found Locale: " + defaultLocale.getLanguage());
            builder.addPreferredLanguage(defaultLocale.getLanguage() + "-" + defaultLocale.getCountry());
            builder.addPreferredLanguage(defaultLocale.getLanguage());

            //
            // Adding TimeZone
            //
            String timeZone = TimeZone.getDefault().getID();
            Log.d(TAG, "Found TimeZone: " + timeZone);
            builder.setTimeZone(timeZone);

            //
            // App Name
            //
            if (customApplicationName != null) {
                builder.setCustomAppName(customApplicationName);
            }

            //
            // Calls Support
            //
            builder.setCallsProvider(new AndroidCallProvider());

            //
            // Handle raw updates
            //
            builder.setRawUpdatesHandler(getDelegate().getRawUpdatesHandler());

            //
            // Auto Join
            //
            for (String s : autoJoinGroups) {
                builder.addAutoJoinGroup(s);
            }
            builder.setAutoJoinType(autoJoinType);

            //
            // Building Messenger
            //
            this.messenger = new AndroidMessenger(application, builder.build());

            //
            // Keep Alive
            //
            if (isKeepAliveEnabled) {
                Intent keepAliveService = new Intent(application, KeepAliveService.class);
                PendingIntent pendingIntent = PendingIntent.getService(application, 0, keepAliveService, 0);
                AlarmManager alarm = (AlarmManager) application.getSystemService(Context.ALARM_SERVICE);
                alarm.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 30 * 1000, pendingIntent);
            }


            //
            // Actor Push
            //
            if (actorPushEndpoint != null) {
                ActorPushRegister.registerForPush(application, actorPushEndpoint, endpoint -> {
                    Log.d(TAG, "On Actor push registered: " + endpoint);
                    messenger.registerActorPush(endpoint);
                });
            }


            //
            // GCM
            //
            try {
                if (pushId != 0) {
                    final ActorPushManager pushManager = (ActorPushManager) Class.forName("im.actor.push.PushManager").newInstance();
                    pushManager.registerPush(application);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            synchronized (LOAD_LOCK) {
                isLoaded = true;
                LOAD_LOCK.notifyAll();
            }

            //
            // Loading Emoji
            //

            emojiProcessor.loadEmoji();
        });
    }

    /**
     * Waiting for Messenger Ready
     */
    public void waitForReady() {
        if (!isLoaded) {
            synchronized (LOAD_LOCK) {
                if (!isLoaded) {
                    try {
                        long start = Runtime.getActorTime();
                        LOAD_LOCK.wait();
                        Log.d(TAG, "Waited for startup in " + (Runtime.getActorTime() - start) + " ms");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Call this method for staring messaging app
     *
     * @param context Current Activity
     */
    public void startMessagingApp(Activity context) {
        if (messenger.isLoggedIn()) {
            startMessagingActivity(context);
        } else {
            startAuthActivity(context);
        }
    }

    //
    // Getters/Setters
    //

    /**
     * Application Context
     *
     * @return Application Context
     */
    public Application getApplication() {
        return application;
    }

    /**
     * Actor Messenger
     *
     * @return Actor Messenger instance
     */
    public AndroidMessenger getMessenger() {
        return messenger;
    }

    /**
     * Getting Endpoints for SDK
     *
     * @return Endpoints list
     */
    public String[] getEndpoints() {
        return endpoints;
    }

    /**
     * Setting endpoints for SDK
     *
     * @param endpoints Endpoints list
     */
    public void setEndpoints(String[] endpoints) {
        this.endpoints = endpoints;
        this.trustedKeys = new String[0];
    }

    /**
     * Getting Trusted keys for server
     *
     * @return trusted keys
     */
    public String[] getTrustedKeys() {
        return trustedKeys;
    }

    /**
     * Setting Trusted Keys for server
     *
     * @param trustedKeys trusted keys
     */
    public void setTrustedKeys(String[] trustedKeys) {
        this.trustedKeys = trustedKeys;
    }

    /**
     * Getting API id
     *
     * @return API App id
     */
    public int getApiAppId() {
        return apiAppId;
    }

    /**
     * Setting API App id
     *
     * @param apiAppId API App Id
     */
    public void setApiAppId(int apiAppId) {
        this.apiAppId = apiAppId;
    }

    /**
     * Getting API Key
     *
     * @return API Key
     */
    public String getApiAppKey() {
        return apiAppKey;
    }

    /**
     * Setting API App Key
     *
     * @param apiAppKey API Key
     */
    public void setApiAppKey(String apiAppKey) {
        this.apiAppKey = apiAppKey;
    }

    /**
     * Is Keeping Alive enabled. Keeping Android Application always online for
     * providing better notifications
     *
     * @return Is Enabled
     */
    public boolean isKeepAliveEnabled() {
        return isKeepAliveEnabled;
    }

    /**
     * Setting Keeping Alive enabled
     *
     * @param isKeepAliveEnabled Is Enabled
     */
    public void setIsKeepAliveEnabled(boolean isKeepAliveEnabled) {
        this.isKeepAliveEnabled = isKeepAliveEnabled;
    }

    /**
     * Getting custom application name
     *
     * @return application name if set
     */
    public String getCustomApplicationName() {
        return customApplicationName;
    }

    /**
     * Setting custom application name
     *
     * @param customApplicationName new application name
     */
    public void setCustomApplicationName(String customApplicationName) {
        this.customApplicationName = customApplicationName;
    }

    /**
     * Getting Application Name. Used to identify application.
     *
     * @return Application Name
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Setting Application Name.
     *
     * @param appName Application name
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
     * Getting Group Invite Prefix
     *
     * @return group invite prefix
     */
    @Nullable
    public String getGroupInvitePrefix() {
        return groupInvitePrefix;
    }

    /**
     * Setting Group Invite Prefix
     *
     * @param groupInvitePrefix group invite prefix
     */
    public void setGroupInvitePrefix(@Nullable String groupInvitePrefix) {
        this.groupInvitePrefix = groupInvitePrefix;
    }

    /**
     * Getting Push Registration Id
     *
     * @return pushId
     */
    public long getPushId() {
        return pushId;
    }

    /**
     * Setting Push Registration Id
     */
    public void setPushId(long pushId) {
        this.pushId = pushId;
    }

    /**
     * Getting Actor Push notification service endpoint
     *
     * @return endpoint
     */
    public String getActorPushEndpoint() {
        return actorPushEndpoint;
    }

    /**
     * Setting Actor push notification service endpoint
     *
     * @param actorPushEndpoint endpoint
     */
    public void setActorPushEndpoint(String actorPushEndpoint) {
        this.actorPushEndpoint = actorPushEndpoint;
    }

    /**
     * Getting account support phone
     *
     * @return account support phone
     */
    public String getHelpPhone() {
        return helpPhone;
    }

    /**
     * Setting account support phone
     *
     * @param helpPhone account support phone
     */
    public void setHelpPhone(String helpPhone) {
        this.helpPhone = helpPhone;
    }

    /**
     * Getting app home page
     *
     * @return app home page
     */
    public String getHomePage() {
        return homePage;
    }

    /**
     * Setting app home page
     *
     * @param homePage app home page
     */
    public void setHomePage(String homePage) {
        this.homePage = homePage;
    }

    /**
     * Getting app twitter account
     *
     * @return app twitter account
     */
    public String getTwitterAcc() {
        return twitter;
    }

    /**
     * Setting app twitter account
     *
     * @param twitter app twitter account
     */
    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    /**
     * Is fast share menu enabled
     *
     * @return fastShareEnabled is fast share enabled
     */
    public boolean isFastShareEnabled() {
        return fastShareEnabled;
    }

    /**
     * Setting is is fast share enabled - experimental feature, disabled by default
     *
     * @param fastShareEnabled is fast share enabled
     */
    public void setFastShareEnabled(boolean fastShareEnabled) {
        this.fastShareEnabled = fastShareEnabled;
    }

    /**
     * Setting is calls enabled - if enabled app will handle calls updates e.g. UpdateIncomingCall/UpdateCallSignal/UpdateCallEnded etc
     *
     * @param callsEnabled is calls enabled
     */
    public void setCallsEnabled(boolean callsEnabled) {
        this.callsEnabled = callsEnabled;
    }

    /**
     * Alternate endpoints - allow choose alternate endpoint on auth - disabled be default
     *
     * @return is isUseAlternateEndpointsEnabled enabled
     */
    public boolean isUseAlternateEndpointsEnabled() {
        return useAlternateEndpoints;
    }

    /**
     * Is alternate endpoints choose enabled
     *
     * @param useAlternateEndpoints is setUseAlternateEndpoints enabled
     */
    public void setUseAlternateEndpoints(boolean useAlternateEndpoints) {
        this.useAlternateEndpoints = useAlternateEndpoints;
    }

    /**
     * Is calls enabled.
     *
     * @return callsEnabled is calls enabled
     */
    public boolean isCallsEnabled() {
        return callsEnabled;
    }

    /**
     * Getting Application Delegate.
     *
     * @return Application Delegate
     */
    @NotNull
    public ActorSDKDelegate getDelegate() {
        return delegate;
    }

    /**
     * Getting url for invite sharing
     *
     * @return invite url
     */
    public String getInviteUrl() {
        return inviteUrl;
    }

    /**
     * Setting url for invite sharing
     *
     * @param inviteUrl invite url
     */
    public void setInviteUrl(String inviteUrl) {
        this.inviteUrl = inviteUrl;
    }

    /**
     * Getting binary mask for auth type
     *
     * @return binary mask for auth type
     */
    public int getAuthType() {
        return authType;
    }

    /**
     * Setting binary mask for auth type
     * available auth types are: {@link AuthActivity#AUTH_TYPE_EMAIL}, {@link AuthActivity#AUTH_TYPE_PHONE}
     *
     * @param authType - binary mask for auth type
     */
    public void setAuthType(int authType) {
        this.authType = authType;
    }

    /**
     * Getting terms of service url
     *
     * @return terms of service url
     */
    public String getTosUrl() {
        return tosUrl;
    }

    /**
     * Setting terms of service url
     *
     * @param tosUrl terms of service url
     */
    public void setTosUrl(String tosUrl) {
        this.tosUrl = tosUrl;
    }

    /**
     * Getting terms of service text
     *
     * @return terms of service text
     */
    public String getTosText() {
        return tosText;
    }

    /**
     * Setting terms of service text
     *
     * @param tosText terms of service text
     */
    public void setTosText(String tosText) {
        this.tosText = tosText;
    }

    /**
     * Getting privacy policy url
     *
     * @return privacy policy url
     */
    public String getPrivacyUrl() {
        return privacyUrl;
    }

    /**
     * Setting privacy policy url
     *
     * @param privacyUrl terms of service url
     */
    public void setPrivacyUrl(String privacyUrl) {
        this.privacyUrl = privacyUrl;
    }

    /**
     * Getting privacy policy text
     *
     * @return privacy policy text
     */
    public String getPrivacyText() {
        return privacyText;
    }

    /**
     * Setting privacy policy text
     *
     * @param privacyText privacy policy text
     */
    public void setPrivacyText(String privacyText) {
        this.privacyText = privacyText;
    }


    /**
     * Get Current Auto Join group tokens
     *
     * @return auto join tokens
     */
    public String[] getAutoJoinGroups() {
        return autoJoinGroups;
    }

    /**
     * Set Auto Join group tokens
     *
     * @param autoJoinGroups auto join tokens
     */
    public void setAutoJoinGroups(String[] autoJoinGroups) {
        this.autoJoinGroups = autoJoinGroups;
    }

    /**
     * Set auto join type
     *
     * @return auto join type
     */
    public AutoJoinType getAutoJoinType() {
        return autoJoinType;
    }

    /**
     * Set auto join type
     *
     * @param autoJoinType auto join type
     */
    public void setAutoJoinType(AutoJoinType autoJoinType) {
        this.autoJoinType = autoJoinType;
    }

    /**
     * Setting Application Delegate. Useful for hacking various parts of SDK
     *
     * @param delegate Application Delegate
     */
    public void setDelegate(@NotNull ActorSDKDelegate delegate) {
        this.delegate = delegate;
    }

    /**
     * Method is used internally for starting default activity or activity added in delegate
     *
     * @param context current context
     */
    public void startAuthActivity(Context context) {
        startAuthActivity(context, null);
    }

    /**
     * Method is used internally for starting default activity or activity added in delegate
     *
     * @param context current context
     * @param extras  activity extras
     */
    public void startAuthActivity(Context context, Bundle extras) {
        if (!startDelegateActivity(context, delegate.getAuthStartIntent(), extras)) {
            startActivity(context, extras, AuthActivity.class);
        }
    }

    /**
     * Method is used internally for starting default activity or activity added in delegate
     *
     * @param context current context
     */
    public void startAfterLoginActivity(Context context) {
        startAfterLoginActivity(context, null);
    }

    /**
     * Method is used internally for starting default activity or activity added in delegate
     *
     * @param context current context
     * @param extras  activity extras
     */
    public void startAfterLoginActivity(Context context, Bundle extras) {
        if (!startDelegateActivity(context, delegate.getStartAfterLoginIntent(), extras)) {
            startMessagingActivity(context, extras);
        }
    }

    /**
     * Method is used internally for starting default activity or activity added in delegate
     *
     * @param context current context
     */
    public void startMessagingActivity(Context context) {
        startMessagingActivity(context, null);
    }

    /**
     * Method is used internally for starting default activity or activity added in delegate
     *
     * @param context current context
     * @param extras  activity extras
     */
    public void startMessagingActivity(Context context, Bundle extras) {
        if (!startDelegateActivity(context, delegate.getStartIntent(), extras)) {
            startActivity(context, extras, RootActivity.class);
        }
    }

    /**
     * Method is used internally for starting default activity or activity added in delegate
     *
     * @param context current context
     */
    public void startSettingActivity(Context context) {
        startSettingActivity(context, null);
    }

    /**
     * Method is used internally for starting default activity or activity added in delegate
     *
     * @param context current context
     * @param extras  activity extras
     */
    public void startSettingActivity(Context context, Bundle extras) {
        if (!startDelegateActivity(context, delegate.getSettingsIntent(), extras)) {
            startActivity(context, extras, MyProfileActivity.class);
        }
    }


    /**
     * Method is used internally for starting default activity or activity added in delegate
     *
     * @param context current context
     * @param gid     group id
     */
    public void startGroupInfoActivity(Context context, int gid) {
        Bundle b = new Bundle();
        b.putInt(Intents.EXTRA_GROUP_ID, gid);
        startActivity(context, b, GroupInfoActivity.class);
    }

    /**
     * Method is used internally for starting default activity or activity added in delegate
     *
     * @param context current context
     */
    public void startSecuritySettingsActivity(Context context) {
        if (!startDelegateActivity(context, delegate.getSecuritySettingsIntent(), null)) {
            startActivity(context, null, SecuritySettingsActivity.class);
        }
    }

    /**
     * Method is used internally for starting default activity or activity added in delegate
     *
     * @param context current context
     */
    public void startChatActivity(Context context, Peer peer, boolean compose) {
        Bundle b = new Bundle();
        b.putLong(Intents.EXTRA_CHAT_PEER, peer.getUnuqueId());
        b.putBoolean(Intents.EXTRA_CHAT_COMPOSE, compose);
        if (!startDelegateActivity(context, delegate.getChatIntent(peer, compose), b, new int[]{Intent.FLAG_ACTIVITY_SINGLE_TOP})) {
            startActivity(context, b, ChatActivity.class);
        }
    }

    private boolean startDelegateActivity(Context context, ActorIntent intent, Bundle extras) {
        return startDelegateActivity(context, intent, extras, new int[]{});
    }

    private boolean startDelegateActivity(Context context, ActorIntent intent, Bundle extras, int[] flags) {
        if (intent != null && intent instanceof ActorIntentActivity) {
            Intent startIntent = ((ActorIntentActivity) intent).getIntent();

            if (startIntent != null) {

                for (int flag : flags) {
                    startIntent.addFlags(flag);
                }
                if (extras != null) {
                    startIntent.putExtras(extras);
                }

                context.startActivity(startIntent);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void startActivity(Context context, Bundle extras, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    /**
     * Method is used internally for getting delegated fragment
     */
    public <T> T getDelegatedFragment(ActorIntent delegatedIntent, android.support.v4.app.Fragment baseFragment, Class<T> type) {

        if (delegatedIntent != null &&
                delegatedIntent instanceof ActorIntentFragmentActivity &&
                ((ActorIntentFragmentActivity) delegatedIntent).getFragment() != null
                && type.isInstance(((ActorIntentFragmentActivity) delegatedIntent).getFragment())) {
            return (T) ((ActorIntentFragmentActivity) delegatedIntent).getFragment();
        } else {
            return (T) baseFragment;
        }

    }


    public boolean isVideoCallsEnabled() {
        return videoCallsEnabled;
    }

    public void setVideoCallsEnabled(boolean videoCallsEnabled) {
        this.videoCallsEnabled = videoCallsEnabled;
    }

    public void setOnClientPrivacyEnabled(boolean onClientPrivacyEnabled) {
        this.onClientPrivacyEnabled = onClientPrivacyEnabled;
    }

    public boolean isOnClientPrivacyEnabled() {
        return onClientPrivacyEnabled;
    }

    public String getInviteDataUrl() {
        return inviteDataUrl;
    }

    public void setInviteDataUrl(String inviteDataUrl) {
        this.inviteDataUrl = inviteDataUrl;
    }


    public static void returnToRoot(Context context) {
        Intent i;
        ActorIntent startIntent = ActorSDK.sharedActor().getDelegate().getStartIntent();
        if (startIntent != null && startIntent instanceof ActorIntentActivity) {
            i = ((ActorIntentActivity) startIntent).getIntent();
        } else {
            i = new Intent(context, RootActivity.class);
        }
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }
}
