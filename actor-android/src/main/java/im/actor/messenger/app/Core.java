package im.actor.messenger.app;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Base64;

import im.actor.images.cache.BitmapClasificator;
import im.actor.images.loading.ImageLoader;

import im.actor.messenger.BuildConfig;
import im.actor.messenger.app.emoji.EmojiProcessor;
import im.actor.messenger.app.images.AvatarActor;
import im.actor.messenger.app.images.AvatarTask;
import im.actor.messenger.app.images.FullAvatarActor;
import im.actor.messenger.app.images.FullAvatarTask;
import im.actor.messenger.app.images.ImagePreviewActor;
import im.actor.messenger.app.images.ImagePreviewTask;
import im.actor.messenger.app.images.VideoActor;
import im.actor.messenger.app.images.VideoPreviewActor;
import im.actor.messenger.app.images.VideoPreviewTask;
import im.actor.messenger.app.images.VideoTask;
import im.actor.model.ApiConfiguration;
import im.actor.model.ConfigurationBuilder;
import im.actor.model.android.AndroidCallbackDispatcher;
import im.actor.model.android.AndroidFileProvider;
import im.actor.model.android.AndroidLog;
import im.actor.model.android.AndroidMainThreadProvider;
import im.actor.model.android.AndroidNotifications;
import im.actor.model.android.AndroidPhoneBook;
import im.actor.model.android.AndroidStorageProvider;
import im.actor.model.crypto.bouncycastle.BouncyCastleProvider;
import im.actor.model.entity.Group;
import im.actor.model.entity.User;
import im.actor.model.jvm.JavaLocale;
import im.actor.model.jvm.JavaNetworkProvider;
import im.actor.model.jvm.JavaThreadingProvider;
import im.actor.model.mvvm.MVVMCollection;
import im.actor.model.viewmodel.GroupVM;
import im.actor.model.viewmodel.UserVM;

import static com.droidkit.actors.ActorSystem.system;

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

    private ImageLoader imageLoader;
    private EmojiProcessor emojiProcessor;
    private im.actor.model.android.AndroidMessenger messenger;

    private Core(Application application) {

        // Helpers
        AppContext.setContext(application);

        // Init actor system
        system().setClassLoader(AppContext.getContext().getClassLoader());
        system().addDispatcher("db", 1);

        // Emoji
        // this.emojiProcessor = new EmojiProcessor(application);

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
        this.imageLoader = new ImageLoader(clasificator, application);
        this.imageLoader.getTaskResolver().register(ImagePreviewTask.class, ImagePreviewActor.class);
        this.imageLoader.getTaskResolver().register(VideoPreviewTask.class, VideoPreviewActor.class);
        this.imageLoader.getTaskResolver().register(VideoTask.class, VideoActor.class);
        this.imageLoader.getTaskResolver().register(AvatarTask.class, AvatarActor.class);
        this.imageLoader.getTaskResolver().register(FullAvatarTask.class, FullAvatarActor.class);


        ConfigurationBuilder builder = new ConfigurationBuilder();
        BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
        builder.setThreadingProvider(new JavaThreadingProvider());
        builder.setNetworkProvider(new JavaNetworkProvider());
        builder.setMainThreadProvider(new AndroidMainThreadProvider());
        builder.setLog(new AndroidLog());
        builder.setStorage(new AndroidStorageProvider());
        builder.setLocale(new JavaLocale("En"));
        builder.setPhoneBookProvider(new AndroidPhoneBook());
        // builder.setCryptoProvider(new AndroidCryptoProvider());
        builder.setFileSystemProvider(new AndroidFileProvider(application));
        builder.setNotificationProvider(new AndroidNotifications());
        builder.setDispatcherProvider(new AndroidCallbackDispatcher());

        if (BuildConfig.API_SSL) {
            builder.addEndpoint("tls://" + BuildConfig.API_HOST + ":" + BuildConfig.API_PORT);
        } else {
            builder.addEndpoint("tcp://" + BuildConfig.API_HOST + ":" + BuildConfig.API_PORT);
        }
        builder.setEnableContactsLogging(true);
        builder.setEnableNetworkLogging(true);

        byte[] deviceHash = bouncyCastleProvider.SHA256(Base64.encode((AppContext.getContext().getPackageName() + ":" + Build.SERIAL).getBytes(),
                Base64.DEFAULT));

        builder.setApiConfiguration(new ApiConfiguration("Actor Android v0.1", 1, "??", "Android Device",
                deviceHash));
        builder.setCryptoProvider(bouncyCastleProvider);

        this.messenger = new im.actor.model.android.AndroidMessenger(builder.build());

        // Bind phone book change
        AppContext.getContext()
                .getContentResolver()
                .registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true,
                        new ContentObserver(null) {
                            @Override
                            public void onChange(boolean selfChange) {
                                messenger.onPhoneBookChanged();
                            }
                        });
    }

    public static EmojiProcessor emoji() {
        return core().emojiProcessor;
    }

    public static int myUid() {
        return core().messenger.myUid();
    }

    public static ImageLoader getImageLoader() {
        return core().imageLoader;
    }

    public static im.actor.model.android.AndroidMessenger messenger() {
        return core().messenger;
    }

    public static MVVMCollection<User, UserVM> users() {
        return core().messenger.getUsers();
    }

    public static MVVMCollection<Group, GroupVM> groups() {
        return core().messenger.getGroups();
    }
}
