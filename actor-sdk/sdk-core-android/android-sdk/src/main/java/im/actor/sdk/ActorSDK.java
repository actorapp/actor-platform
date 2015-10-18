package im.actor.sdk;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.AndroidMessenger;
import im.actor.core.ApiConfiguration;
import im.actor.core.ConfigurationBuilder;
import im.actor.core.DeviceCategory;
import im.actor.core.PlatformType;
import im.actor.messenger.app.activity.ActorMainActivity;
import im.actor.sdk.core.AndroidNotifications;
import im.actor.sdk.core.AndroidPhoneBook;
import im.actor.messenger.app.core.KeepAliveService;
import im.actor.messenger.app.util.Devices;
import im.actor.messenger.app.view.emoji.SmileProcessor;
import im.actor.runtime.android.AndroidContext;

public class ActorSDK {


    /**
     * Shared ActorSDK. Use this method to get instance of SDK for configuration and starting up
     *
     * @return ActorSDK instance.
     */
    public static ActorSDK sharedActor() {
        // Use function if we will replace implementation for some cases
        return sdk;
    }

    private static volatile ActorSDK sdk = new ActorSDK();


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
     * Is Keeping app alive enabled
     */
    private boolean isKeepAliveEnabled = false;


    private ActorSDK() {
        endpoints.add("tls://front1-mtproto-api-rev2.actor.im");
        endpoints.add("tls://front2-mtproto-api-rev2.actor.im");
    }

    //
    // SDK Initialization
    //

    public void createActor(Application application) {

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
                // TODO: Update Title
                "Title?",
                apiAppId,
                apiAppKey,
                Devices.getDeviceName(),
                AndroidContext.getContext().getPackageName() + ":" + Build.SERIAL));
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
    }

    public void startMessagingApp(Activity context) {
        context.startActivity(new Intent(AndroidContext.getContext(), ActorMainActivity.class));
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
}
