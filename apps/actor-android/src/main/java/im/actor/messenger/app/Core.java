package im.actor.messenger.app;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.splunk.mint.Mint;

import im.actor.android.AndroidConfigurationBuilder;
import im.actor.android.AndroidMixpanelAnalytics;
import im.actor.images.cache.BitmapClasificator;
import im.actor.images.loading.ImageLoader;
import im.actor.messenger.BuildConfig;
import im.actor.messenger.R;
import im.actor.messenger.app.emoji.SmileProcessor;
import im.actor.messenger.app.emoji.StickerProcessor;
import im.actor.messenger.app.images.FullAvatarActor;
import im.actor.messenger.app.images.FullAvatarTask;
import im.actor.messenger.app.service.KeepAliveService;
import im.actor.model.ApiConfiguration;
import im.actor.model.android.AndroidMessenger;
import im.actor.model.android.providers.AndroidNotifications;
import im.actor.model.android.providers.AndroidPhoneBook;
import im.actor.model.entity.Group;
import im.actor.model.entity.User;
import im.actor.model.mvvm.MVVMCollection;
import im.actor.model.providers.EmptyPhoneProvider;
import im.actor.model.viewmodel.GroupVM;
import im.actor.model.viewmodel.UserVM;

/**
 * Created by ex3ndr on 30.08.14.
 */
public class Core {

    private static volatile Core core;

    public static void init(Application application) {
        core = new Core(application);
    }

    public static Core core() {
        if (core == null) {
            throw new RuntimeException("Core is not initialized");
        }

        return core;
    }

    private final SmileProcessor smileProcessor;
    private final StickerProcessor stickerProcessor;
    private ImageLoader imageLoader;
    private AndroidMessenger messenger;

    private Core(Application application) {

        // Integrations
        if (BuildConfig.MINT != null) {
            Mint.disableNetworkMonitoring();
            Mint.initAndStartSession(application, BuildConfig.MINT);
        }
        Fresco.initialize(application);

        // Keep Alive
        application.startService(new Intent(application, KeepAliveService.class));

        // Helpers
        AppContext.setContext(application);

        // Init Image Engine
        ActivityManager activityManager = (ActivityManager) AppContext.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        int memoryInMB = Math.min(activityManager.getMemoryClass(), 32);
        long totalAppHeap = memoryInMB * 1024 * 1024;
        int cacheLimit = (int) totalAppHeap / 4;
        int freeCacheLimit = cacheLimit / 2;

        BitmapClasificator clasificator = new BitmapClasificator.Builder()

                .startExactSize(100, 100)
                .setFreeSize(2)
                .setLruSize(15)
                .endFilter()

                .startLessOrEqSize(90, 90)
                .setFreeSize(10)
                .useSizeInAmount()
                .endFilter()

                .startAny()
                .useSizeInBytes()
                .setLruSize(cacheLimit)
                .setFreeSize(freeCacheLimit)
                .endFilter()

                .build();

        this.smileProcessor = new SmileProcessor(application);
        this.smileProcessor.loadEmoji();

        this.stickerProcessor = new StickerProcessor(application);
        this.stickerProcessor.loadStickers();

        this.imageLoader = new ImageLoader(clasificator, application);
        this.imageLoader.getTaskResolver().register(FullAvatarTask.class, FullAvatarActor.class);

        AndroidConfigurationBuilder builder = new AndroidConfigurationBuilder(
                application.getResources().getString(R.string.app_locale),
                application);
        if (BuildConfig.ENABLE_PHONE_BOOK) {
            builder.setPhoneBookProvider(new AndroidPhoneBook());
        } else {
            builder.setPhoneBookProvider(new EmptyPhoneProvider());
        }
        builder.setNotificationProvider(new AndroidNotifications(AppContext.getContext()));
        for (String url : BuildConfig.API_URL) {
            builder.addEndpoint(url);
        }
        builder.setEnableContactsLogging(true);
        builder.setEnableNetworkLogging(true);
        builder.setEnableFilesLogging(true);
        builder.setAnalyticsProvider(new AndroidMixpanelAnalytics(AppContext.getContext(), "b2b7a96c3f1e131cf170029f97b2c7c2"));

        builder.setApiConfiguration(new ApiConfiguration(
                BuildConfig.VERSION_TITLE,
                BuildConfig.API_ID,
                BuildConfig.API_KEY,
                "Android Device",
                AppContext.getContext().getPackageName() + ":" + Build.SERIAL));

        this.messenger = new AndroidMessenger(AppContext.getContext(), builder.build());
    }

    public static int myUid() {
        return core().messenger.myUid();
    }

    public static StickerProcessor getStickerProcessor() {
        return core().stickerProcessor;
    }

    public static SmileProcessor getSmileProcessor() {
        return core().smileProcessor;
    }

    public static ImageLoader getImageLoader() {
        return core().imageLoader;
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
