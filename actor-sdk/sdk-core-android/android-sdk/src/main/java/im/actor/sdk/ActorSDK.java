package im.actor.sdk;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import im.actor.core.AndroidMessenger;
import im.actor.core.ApiConfiguration;
import im.actor.core.ConfigurationBuilder;
import im.actor.core.DeviceCategory;
import im.actor.core.PlatformType;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.modules.events.IncomingCall;
import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorContext;
import im.actor.runtime.android.view.BindedViewHolder;
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;
import im.actor.sdk.controllers.activity.ActorMainActivity;
import im.actor.sdk.controllers.conversation.messages.MessageHolder;
import im.actor.sdk.controllers.conversation.messages.MessagesAdapter;
import im.actor.sdk.controllers.fragment.auth.AuthActivity;
import im.actor.sdk.controllers.fragment.dialogs.DialogHolder;
import im.actor.sdk.controllers.fragment.settings.MyProfileActivity;
import im.actor.sdk.core.AndroidNotifications;
import im.actor.sdk.core.AndroidPhoneBook;
import im.actor.sdk.core.ActorPushManager;
import im.actor.sdk.intents.ActivityManager;
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
     * Delegate
     */
    @NotNull
    private ActorSDKDelegate delegate = new BaseActorSDKDelegate();
    /**
     * ActivityManager
     */
    private ActivityManager activityManager = new ActivityManager();

    private ActorSDK() {
        endpoints = new String[]{
                "tls://front1-mtproto-api-rev2.actor.im",
                "tls://front2-mtproto-api-rev2.actor.im",

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

        //
        // SDK Tools
        //

        Fresco.initialize(application);
        AndroidContext.setContext(application);
        // TODO: Replace
        new SmileProcessor(application).loadEmoji();

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
        builder.setNotificationProvider(new AndroidNotifications(AndroidContext.getContext()));
        builder.setDeviceCategory(DeviceCategory.MOBILE);
        builder.setPlatformType(PlatformType.ANDROID);
        builder.setApiConfiguration(new ApiConfiguration(
                appName,
                apiAppId,
                apiAppKey,
                Devices.getDeviceName(),
                AndroidContext.getContext().getPackageName() + ":" + Build.SERIAL));

        // Adding Locales
        Locale defaultLocale = Locale.getDefault();
        Log.d(TAG, "Found Locale: " + defaultLocale.getLanguage() + "-" + defaultLocale.getCountry());
        Log.d(TAG, "Found Locale: " + defaultLocale.getLanguage());
        builder.addPreferredLanguage(defaultLocale.getLanguage() + "-" + defaultLocale.getCountry());
        builder.addPreferredLanguage(defaultLocale.getLanguage());

        // Adding TimeZone
        String timeZone = TimeZone.getDefault().getID();
        Log.d(TAG, "Found TimeZone: " + timeZone);
        builder.setTimeZone(timeZone);

        // App Name
        if (customApplicationName != null) {
            builder.setCustomAppName(customApplicationName);
        }

        this.messenger = new AndroidMessenger(AndroidContext.getContext(), builder.build());

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
            ActorPushRegister.registerForPush(application, actorPushEndpoint, new ActorPushRegister.Callback() {
                @Override
                public void onRegistered(String endpoint) {
                    Log.d(TAG, "On Actor push registered: " + endpoint);
                    messenger.registerActorPush(endpoint);
                }
            });
        }

        //
        // GCM
        //

        try {
            final ActorPushManager pushManager = (ActorPushManager) Class.forName("im.actor.push.PushManager").newInstance();
            if (pushId != 0) {
                pushManager.registerPush(application);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //
        // Calls subscribing
        //

        messenger.getEvents().subscribe(new BusSubscriber() {
            @Override
            public void onBusEvent(Event event) {
                delegate.onIncominCall(((IncomingCall) event).getCall(), ((IncomingCall) event).getUid());
            }
        }, IncomingCall.EVENT);
    }

    /**
     * Call this method for staring messaging app
     *
     * @param context
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

    public String getHelpPhone() {
        return helpPhone;
    }

    public void setHelpPhone(String helpPhone) {
        this.helpPhone = helpPhone;
    }

    public String getHomePage() {
        return homePage;
    }

    public void setHomePage(String homePage) {
        this.homePage = homePage;
    }

    public String getTwitterAcc() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
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
     * Setting Application Delegate. Useful for hacking various parts of SDK
     *
     * @param delegate Application Delegate
     */
    public void setDelegate(@NotNull ActorSDKDelegate delegate) {
        this.delegate = delegate;
    }

    @Deprecated
    public ActivityManager getActivityManager() {
        return activityManager;
    }

    public void startAuthActivity(Context context) {
        startAuthActivity(context, null);
    }


    public void startAuthActivity(Context context, Bundle extras) {
        if (!startDelegateActivity(context, delegate.getAuthStartIntent(), extras)) {
            startActivity(context, extras, AuthActivity.class);
        }
    }

    public void startAfterLoginActivity(Context context) {
        startAfterLoginActivity(context, null);
    }

    public void startAfterLoginActivity(Context context, Bundle extras) {
        if (!startDelegateActivity(context, delegate.getStartAfterLoginIntent(), extras)) {
            startMessagingActivity(context, extras);
        }
    }

    public void startMessagingActivity(Context context) {
        startMessagingActivity(context, null);
    }

    public void startMessagingActivity(Context context, Bundle extras) {
        if (!startDelegateActivity(context, delegate.getStartIntent(), extras)) {
            startActivity(context, extras, ActorMainActivity.class);
        }
    }

    public void startSettingActivity(Context context) {
        startSettingActivity(context, null);
    }

    public void startSettingActivity(Context context, Bundle extras) {
        if (!startDelegateActivity(context, delegate.getSettingsIntent(), extras)) {
            startActivity(context, extras, MyProfileActivity.class);
        }
    }


    private boolean startDelegateActivity(Context context, ActorIntent intent, Bundle extras) {
        if (intent != null && intent instanceof ActorIntentActivity) {
            Intent startIntent = ((ActorIntentActivity) intent).getIntent();
            if (extras != null) {
                startIntent.putExtras(extras);
            }
            if (startIntent != null) {
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

    public <T extends BindedViewHolder> T getDelegatedViewHolder(Class<T> base, OnDelegateViewHolder<T> callback, Object... args) {
        T delegated = delegate.getViewHolder(base, args);
        if (delegated != null) {
            return delegated;
        } else {
            return callback.onNotDelegated();
        }
    }

    public MessageHolder getDelegatedMessageViewHolder(int id, OnDelegateViewHolder<MessageHolder> callback, MessagesAdapter messagesAdapter, ViewGroup viewGroup) {
        MessageHolder delegated = delegate.getCustomMessageViewHolder(id, messagesAdapter, viewGroup);
        if (delegated != null) {
            return delegated;
        } else {
            return callback.onNotDelegated();
        }
    }

    public String getInviteUrl() {
        return inviteUrl;
    }

    public void setInviteUrl(String inviteUrl) {
        this.inviteUrl = inviteUrl;
    }

    public interface OnDelegateViewHolder<T> {
        T onNotDelegated();

    }

    public interface OnDeligateMessageHolder {
        MessageHolder onNotDelegated();

        View getItemView();
    }

}
