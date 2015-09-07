package im.actor.messenger.app.core;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.splunk.mint.Mint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import im.actor.core.AndroidMessenger;
import im.actor.core.AndroidPushActor;
import im.actor.core.ApiConfiguration;
import im.actor.core.PlatformType;
import im.actor.core.ConfigurationBuilder;
import im.actor.core.DeviceCategory;
import im.actor.core.entity.Group;
import im.actor.core.entity.User;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.messenger.BuildConfig;
import im.actor.messenger.app.AppContext;
import im.actor.messenger.app.view.emoji.SmileProcessor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.runtime.android.AndroidContext;
import im.actor.runtime.mvvm.MVVMCollection;

import static im.actor.core.utils.IOUtils.readAll;
import static im.actor.runtime.actors.ActorSystem.system;

/**
 * Created by ex3ndr on 30.08.14.
 */
public class Core {

    private static final int API_ID = 1;
    private static final String API_KEY = "4295f9666fad3faf2d04277fe7a0c40ff39a85d313de5348ad8ffa650ad71855";
    public static final int MAX_DELAY = 15000 * 60;
    public static long PUSH_ID;
    private ActorRef androidPushesActor;

    private static volatile Core core;

    public static void init(Application application) {
        try {
            core = new Core(application);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static Core core() {
        if (core == null) {
            throw new RuntimeException("Core is not initialized");
        }

        return core;
    }

    private final SmileProcessor smileProcessor;
    // private final StickerProcessor stickerProcessor;
    private AndroidMessenger messenger;

    private Core(final Application application) throws IOException, JSONException {

        AndroidContext.setContext(application);

        // Integrations
        //noinspection ConstantConditions
        JSONObject config = new JSONObject(new String(readAll(application.getAssets().open("app.json"))));

        if (config.optString("push_id") != null && !config.optString("push_id").equals("null")) {
            PUSH_ID = config.getLong("push_id");
        }

        if (config.optString("mint") != null && !config.optString("mint").equals("null")) {
            Mint.disableNetworkMonitoring();
            Mint.initAndStartSession(application, config.getString("mint"));
        }
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

//        AndroidConfigurationBuilder builder = new AndroidConfigurationBuilder(
//                application.getResources().getString(R.string.app_locale),
//                application);
//        builder.setPhoneBookProvider(new AndroidPhoneBook());
//        builder.setNotificationProvider(new AndroidNotifications(AppContext.getContext()));
//        JSONArray endpoints = config.getJSONArray("endpoints");
//        for (int i = 0; i < endpoints.length(); i++) {
//            builder.addEndpoint(endpoints.getString(i));
//        }
//        builder.setEnableContactsLogging(true);
//        builder.setEnableNetworkLogging(true);
//        builder.setEnableFilesLogging(true);
//        //noinspection ConstantConditions
//        if (config.optString("mixpanel") != null) {
//            builder.setAnalyticsProvider(new AndroidMixpanelAnalytics(AppContext.getContext(), config.getString("mixpanel")));
//        }
//        builder.setDeviceCategory(DeviceCategory.MOBILE);
//        builder.setAppCategory(AppCategory.ANDROID);
//
//        builder.setApiConfiguration(new ApiConfiguration(
//                BuildConfig.VERSION_TITLE,
//                API_ID,
//                API_KEY,
//                getDeviceName(),
//                AppContext.getContext().getPackageName() + ":" + Build.SERIAL));
//
//        builder.setMaxDelay(MAX_DELAY);

        ConfigurationBuilder builder = new ConfigurationBuilder();
        JSONArray endpoints = config.getJSONArray("endpoints");
        for (int i = 0; i < endpoints.length(); i++) {
            builder.addEndpoint(endpoints.getString(i));
        }
        builder.setPhoneBookProvider(new AndroidPhoneBook());
        builder.setNotificationProvider(new AndroidNotifications(AppContext.getContext()));
        builder.setDeviceCategory(DeviceCategory.MOBILE);
        builder.setPlatformType(PlatformType.ANDROID);
        builder.setApiConfiguration(new ApiConfiguration(
                BuildConfig.VERSION_TITLE,
                API_ID,
                API_KEY,
                getDeviceName(),
                AppContext.getContext().getPackageName() + ":" + Build.SERIAL));
        this.messenger = new AndroidMessenger(AppContext.getContext(), builder.build());

        //GCM

        this.androidPushesActor = system().actorOf(Props.create(AndroidPushActor.class, new ActorCreator<AndroidPushActor>() {
            @Override
            public AndroidPushActor create() {
                return new AndroidPushActor(application, messenger);
            }
        }), "actor/android/push");

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

    public static int myUid() {
        return core().messenger.myUid();
    }

    public static SmileProcessor getSmileProcessor() {
        return core().smileProcessor;
    }

    public static AndroidMessenger messenger() {
        return core().messenger;
    }

    public static MVVMCollection<User, UserVM> users() {
        return core().messenger.getUsers();
    }

    public static MVVMCollection<Group, GroupVM> groups() {
        return core().messenger.getGroups();
    }
}
