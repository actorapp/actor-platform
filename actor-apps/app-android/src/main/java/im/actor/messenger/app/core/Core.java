package im.actor.messenger.app.core;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.PowerManager;
import android.view.Display;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.splunk.mint.Mint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import im.actor.android.AndroidConfigurationBuilder;
import im.actor.android.AndroidMessenger;
import im.actor.android.AndroidMixpanelAnalytics;
import im.actor.messenger.BuildConfig;
import im.actor.messenger.R;
import im.actor.messenger.app.AppContext;
import im.actor.messenger.app.view.emoji.SmileProcessor;
import im.actor.model.ApiConfiguration;
import im.actor.model.AppCategory;
import im.actor.model.DeviceCategory;
import im.actor.model.android.providers.AndroidNotifications;
import im.actor.model.android.providers.AndroidPhoneBook;
import im.actor.model.entity.Group;
import im.actor.model.entity.User;
import im.actor.model.mvvm.MVVMCollection;
import im.actor.model.viewmodel.GroupVM;
import im.actor.model.viewmodel.UserVM;

import static im.actor.messenger.app.util.io.IOUtils.readAll;

/**
 * Created by ex3ndr on 30.08.14.
 */
public class Core {

    private static final int API_ID = 1;
    private static final String API_KEY = "4295f9666fad3faf2d04277fe7a0c40ff39a85d313de5348ad8ffa650ad71855";
    public static final int MAX_DELAY = 15000 * 60;

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

    private String hockeyToken;
    private final SmileProcessor smileProcessor;
    // private final StickerProcessor stickerProcessor;
    private AndroidMessenger messenger;

    private Core(Application application) throws IOException, JSONException {

        // Integrations
        //noinspection ConstantConditions
        JSONObject config = new JSONObject(new String(readAll(application.getAssets().open("app.json"))));
        hockeyToken = config.optString("hockeyapp");

        if (config.optString("mint") != null && !config.optString("mint").equals("null")) {
            Mint.disableNetworkMonitoring();
            Mint.initAndStartSession(application, config.getString("mint"));
        }
        Fresco.initialize(application);

        // Keep Alive
        application.startService(new Intent(application, KeepAliveService.class));

        // Helpers
        AppContext.setContext(application);

        this.smileProcessor = new SmileProcessor(application);
        this.smileProcessor.loadEmoji();

        AndroidConfigurationBuilder builder = new AndroidConfigurationBuilder(
                application.getResources().getString(R.string.app_locale),
                application);
        builder.setPhoneBookProvider(new AndroidPhoneBook());
        builder.setNotificationProvider(new AndroidNotifications(AppContext.getContext()));
        JSONArray endpoints = config.getJSONArray("endpoints");
        for (int i = 0; i < endpoints.length(); i++) {
            builder.addEndpoint(endpoints.getString(i));
        }
        builder.setEnableContactsLogging(true);
        builder.setEnableNetworkLogging(true);
        builder.setEnableFilesLogging(true);
        //noinspection ConstantConditions
        if (config.optString("mixpanel") != null) {
            builder.setAnalyticsProvider(new AndroidMixpanelAnalytics(AppContext.getContext(), config.getString("mixpanel")));
        }
        builder.setDeviceCategory(DeviceCategory.MOBILE);
        builder.setAppCategory(AppCategory.ANDROID);

        builder.setApiConfiguration(new ApiConfiguration(
                BuildConfig.VERSION_TITLE,
                API_ID,
                API_KEY,
                getDeviceName(),
                AppContext.getContext().getPackageName() + ":" + Build.SERIAL));

        builder.setMaxDelay(MAX_DELAY);

        this.messenger = new AndroidMessenger(AppContext.getContext(), builder.build());

        // Screen changes
        IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
//        application.registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
//                    AppStateBroker.stateBroker().onScreenOn();
//                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
//                    AppStateBroker.stateBroker().onScreenOff();
//                }
//            }
//        }, screenFilter);
//        if (isScreenOn(application)) {
//            AppStateBroker.stateBroker().onScreenOn();
//        } else {
//            AppStateBroker.stateBroker().onScreenOff();
//        }
    }

    public String getHockeyToken() {
        if (hockeyToken != null && hockeyToken.equals("null")) {
            return null;
        }
        return hockeyToken;
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

    public boolean isScreenOn(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            boolean screenOn = false;
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    screenOn = true;
                }
            }
            return screenOn;
        } else {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            return pm.isScreenOn();
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
