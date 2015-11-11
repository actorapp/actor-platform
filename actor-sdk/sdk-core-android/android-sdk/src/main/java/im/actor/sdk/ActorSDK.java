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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import im.actor.core.AndroidMessenger;
import im.actor.core.ApiConfiguration;
import im.actor.core.ConfigurationBuilder;
import im.actor.core.DeviceCategory;
import im.actor.core.PlatformType;
import im.actor.runtime.Log;
import im.actor.sdk.controllers.activity.ActorMainActivity;
import im.actor.sdk.controllers.fragment.auth.AuthActivity;
import im.actor.sdk.core.AndroidNotifications;
import im.actor.sdk.core.AndroidPhoneBook;
import im.actor.sdk.core.ActorPushManager;
import im.actor.sdk.intents.ActivityManager;
import im.actor.sdk.intents.ActorIntent;
import im.actor.sdk.intents.ActorIntentActivity;
import im.actor.sdk.services.KeepAliveService;
import im.actor.sdk.util.Devices;
import im.actor.sdk.view.emoji.SmileProcessor;
import im.actor.runtime.android.AndroidContext;


public class ActorSDK {

    private static final String TAG = "ActorSDK";

    private static volatile ActorSDK sdk = new ActorSDK();
    //
    //ActorStyle
    //
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
    private List<String> endpoints = new ArrayList<String>();
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
     * Is Keeping app alive enabled
     */
    private boolean isKeepAliveEnabled = false;
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
        endpoints.add("tls://front1-mtproto-api-rev2.actor.im");
        endpoints.add("tls://front2-mtproto-api-rev2.actor.im");
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
        //GCM
        //
        try {
            final ActorPushManager pushManager = (ActorPushManager) Class.forName("im.actor.push.PushManager").newInstance();
            if (pushId != 0) {
                pushManager.registerPush(application);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
    public List<String> getEndpoints() {
        return endpoints;
    }

    /**
     * Setting endpoints for SDK
     *
     * @param endpoints Endpoints list
     */
    public void setEndpoints(List<String> endpoints) {
        this.endpoints = endpoints;
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

    private boolean startDelegateActivity(Context context, ActorIntent intent, Bundle extras) {
        if (intent != null && intent instanceof ActorIntentActivity) {
            Intent startIntent = ((ActorIntentActivity) intent).getIntent();
            if (extras != null) {
                startIntent.putExtras(extras);
            }
            context.startActivity(startIntent);
            return true;
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

}
