package im.actor.messenger.core;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.database.ContentObserver;
import android.provider.ContactsContract;

import com.droidkit.images.cache.BitmapClasificator;
import com.droidkit.images.loading.ImageLoader;

import im.actor.messenger.BuildConfig;
import im.actor.messenger.app.emoji.EmojiProcessor;
import im.actor.messenger.app.view.Formatter;
import im.actor.messenger.core.images.AvatarActor;
import im.actor.messenger.core.images.AvatarTask;
import im.actor.messenger.core.images.FullAvatarActor;
import im.actor.messenger.core.images.FullAvatarTask;
import im.actor.messenger.core.images.ImagePreviewActor;
import im.actor.messenger.core.images.ImagePreviewTask;
import im.actor.messenger.core.images.VideoActor;
import im.actor.messenger.core.images.VideoPreviewActor;
import im.actor.messenger.core.images.VideoPreviewTask;
import im.actor.messenger.core.images.VideoTask;
import im.actor.messenger.storage.provider.AppEngineFactory;
import im.actor.model.ConfigurationBuilder;
import im.actor.model.Messenger;
import im.actor.model.android.AndroidCryptoProvider;
import im.actor.model.android.AndroidLog;
import im.actor.model.android.AndroidMainThread;
import im.actor.model.android.AndroidPhoneBook;
import im.actor.model.entity.Group;
import im.actor.model.entity.User;
import im.actor.model.jvm.JavaLocale;
import im.actor.model.jvm.JavaNetworking;
import im.actor.model.jvm.JavaThreading;
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

    public static EmojiProcessor emoji() {
        return core().emojiProcessor;
    }

    public static int myUid() {
        return core().messenger.myUid();
    }

    public static ImageLoader getImageLoader() {
        return core().imageLoader;
    }

    public static Messenger messenger() {
        return core().messenger;
    }

    public static MVVMCollection<User, UserVM> users() {
        return core().messenger.getUsers();
    }

    public static MVVMCollection<Group, GroupVM> groups() {
        return core().messenger.getGroups();
    }

    private ImageLoader imageLoader;
    private EmojiProcessor emojiProcessor;
    private im.actor.model.Messenger messenger;

    private Core(Application application) {

        // Helpers
        AppContext.setContext(application);
        Formatter.init(application);

        // Init actor system
        system().setClassLoader(AppContext.getContext().getClassLoader());
        system().addDispatcher("db", 1);
        system().addDispatcher("contacts", 1);
        system().addDispatcher("file_encryption", 1);
        system().addDispatcher("rsa", 1);
        system().addDispatcher("updates", 1);
        system().addDispatcher("push", 1);

        // Emoji
        this.emojiProcessor = new EmojiProcessor(application);

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

        builder.setThreading(new JavaThreading());
        builder.setNetworking(new JavaNetworking());

        builder.setMainThread(new AndroidMainThread());
        builder.setLog(new AndroidLog());
        builder.setStorage(new AppEngineFactory());

        builder.setLocale(new JavaLocale("En"));
        builder.setPhoneBookProvider(new AndroidPhoneBook());
        builder.setCryptoProvider(new AndroidCryptoProvider());

        if (BuildConfig.API_SSL) {
            builder.addEndpoint("tls://" + BuildConfig.API_HOST + ":" + BuildConfig.API_PORT);
        } else {
            builder.addEndpoint("tcp://" + BuildConfig.API_HOST + ":" + BuildConfig.API_PORT);
        }

        this.messenger = new im.actor.model.Messenger(builder.build());

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
}
