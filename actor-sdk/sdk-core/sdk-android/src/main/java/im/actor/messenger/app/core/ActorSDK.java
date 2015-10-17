package im.actor.messenger.app.core;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.facebook.drawee.backends.pipeline.Fresco;

import org.json.JSONException;

import java.io.IOException;

import im.actor.core.AndroidMessenger;
import im.actor.core.AndroidPushActor;
import im.actor.core.ApiConfiguration;
import im.actor.core.ConfigurationBuilder;
import im.actor.core.DeviceCategory;
import im.actor.core.PlatformType;
import im.actor.core.entity.Group;
import im.actor.core.entity.User;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.messenger.BuildConfig;
import im.actor.messenger.app.AppContext;
import im.actor.messenger.app.activity.ActorMainActivity;
import im.actor.messenger.app.view.emoji.SmileProcessor;
import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.runtime.android.AndroidContext;
import im.actor.runtime.mvvm.MVVMCollection;

import static im.actor.runtime.actors.ActorSystem.system;

public class ActorSDK {
    private static volatile ActorSDK sdk;
    private String[] endpoints = new String[]{"tls://front1-mtproto-api-rev2.actor.im", "tls://front2-mtproto-api-rev2.actor.im"};
    private long gcmProjectId = -1;
    private String versionTitle = "debug";
    private ActorRef androidPushesActor;
    private int AppId = 1;
    private String ApiKey = "4295f9666fad3faf2d04277fe7a0c40ff39a85d313de5348ad8ffa650ad71855";
    private SmileProcessor smileProcessor;
    private AndroidMessenger messenger;


    public void createActor(final Application application) {

        AndroidContext.setContext(application);

        Fresco.initialize(application);

        // Keep Alive
        if (BuildConfig.ENABLE_KEEP_ALIVE) {
            Intent keepAliveService = new Intent(application, KeepAliveService.class);
            PendingIntent pintent = PendingIntent.getService(application, 0, keepAliveService, 0);
            AlarmManager alarm = (AlarmManager) application.getSystemService(Context.ALARM_SERVICE);
            alarm.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 30 * 1000, pintent);
        }

        // Helpers
        AppContext.setContext(application);

        this.smileProcessor = new SmileProcessor(application);
        this.smileProcessor.loadEmoji();


        ConfigurationBuilder builder = new ConfigurationBuilder();

        for (int i = 0; i < endpoints.length; i++) {
            builder.addEndpoint(endpoints[i]);
        }
        builder.setPhoneBookProvider(new AndroidPhoneBook());
        builder.setNotificationProvider(new AndroidNotifications(AppContext.getContext()));
        builder.setDeviceCategory(DeviceCategory.MOBILE);
        builder.setPlatformType(PlatformType.ANDROID);
        builder.setApiConfiguration(new ApiConfiguration(
                getVersionTitle(),
                getAppId(),
                getApiKey(),
                getDeviceName(),
                AppContext.getContext().getPackageName() + ":" + Build.SERIAL));
        this.messenger = new AndroidMessenger(AppContext.getContext(), builder.build());

        if (getGcmProjectId() != -1) {
            androidPushesActor = system().actorOf(Props.create(AndroidPushActor.class, new ActorCreator<AndroidPushActor>() {
                @Override
                public AndroidPushActor create() {
                    return new AndroidPushActor(application, sdk.messenger);
                }
            }), "actor/android/push");
        }

    }

    public static ActorSDK sharedActor() {
        if (sdk == null) {
            sdk = new ActorSDK();
        }
        return sdk;
    }

    public void startMessagingApp() {
        AppContext.getContext().startActivity(new Intent(AppContext.getContext(), ActorMainActivity.class));
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }


    //Getters and setters
    public long getGcmProjectId() {
        return gcmProjectId;
    }

    public static int myUid() {
        return sharedActor().messenger.myUid();
    }

    public static SmileProcessor getSmileProcessor() {
        return sharedActor().smileProcessor;
    }

    public static AndroidMessenger messenger() {
        return sharedActor().messenger;
    }

    public static MVVMCollection<User, UserVM> users() {
        return sharedActor().messenger.getUsers();
    }

    public static MVVMCollection<Group, GroupVM> groups() {
        return sharedActor().messenger.getGroups();
    }

    public static void startMessnger() {
        AppContext.getContext().startActivity(new Intent(AppContext.getContext(), ActorMainActivity.class));
    }

    public String[] getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(String[] endpoints) {
        this.endpoints = endpoints;
    }

    public void setGcmProjectId(long gcmProjectId) {
        this.gcmProjectId = gcmProjectId;
    }

    public String getVersionTitle() {
        return versionTitle;
    }

    public void setVersionTitle(String versionTitle) {
        this.versionTitle = versionTitle;
    }

    public int getAppId() {
        return AppId;
    }

    public void setAppId(int appId) {
        AppId = appId;
    }

    public String getApiKey() {
        return ApiKey;
    }

    public void setApiKey(String apiKey) {
        ApiKey = apiKey;
    }
}
