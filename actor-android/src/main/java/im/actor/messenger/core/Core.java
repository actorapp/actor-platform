package im.actor.messenger.core;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.droidkit.actors.android.AndroidTrace;

import im.actor.messenger.BuildConfig;
import im.actor.messenger.app.emoji.EmojiProcessor;
import im.actor.messenger.app.view.Formatter;

import com.droidkit.actors.android.UiActorDispatcher;
import com.droidkit.images.cache.BitmapClasificator;
import com.droidkit.images.loading.ImageLoader;

import im.actor.messenger.core.images.*;
import im.actor.messenger.model.TypingModel;
import im.actor.messenger.model.UserPresence;
import im.actor.messenger.storage.provider.AppEngineFactory;
import im.actor.messenger.storage.provider.PropertiesProvider;
import im.actor.model.Messenger;
import im.actor.model.OnlineCallback;
import im.actor.model.TypingCallback;
import im.actor.model.concurrency.MainThread;
import im.actor.model.droidkit.actors.conf.EnvConfig;
import im.actor.model.jvm.JavaInit;
import im.actor.model.network.ConnectionEndpoint;
import im.actor.model.network.Endpoints;

import static com.droidkit.actors.ActorSystem.system;
import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 30.08.14.
 */
public class Core {

    public static final int AUTH_STATE = 0x04;

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

    private ImageLoader imageLoader;
    private EmojiProcessor emojiProcessor;
    private im.actor.model.Messenger messenger;

    private Core(Application application) {

        // Helpers
        AppContext.setContext(application);
        Formatter.init(application);

        // Init actor system

        system().setClassLoader(AppContext.getContext().getClassLoader());
        AndroidTrace.initTrace(system(), new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                if (BuildConfig.REPORT_CRASHES) {
                    // Mint.logException(ex);
                }
            }
        });

        system().addDispatcher("ui", new UiActorDispatcher("ui", system()));
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

        JavaInit.init();

        im.actor.model.Configuration configuration = new im.actor.model.Configuration();
        configuration.setMainThread(new MainThread() {

            private Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void runOnUiThread(Runnable runnable) {
                handler.post(runnable);
            }
        });
        PropertiesProvider propertiesProvider = new PropertiesProvider();
        configuration.setOnlineCallback(new OnlineCallback() {
            @Override
            public void onUserOnline(int uid) {
                users().get(uid).getPresence().change(new UserPresence(UserPresence.State.ONLINE, 0));
            }

            @Override
            public void onUserOffline(int uid) {
                users().get(uid).getPresence().change(new UserPresence(UserPresence.State.OFFLINE, 0));
            }

            @Override
            public void onUserLastSeen(int uid, long lastSeen) {
                users().get(uid).getPresence().change(new UserPresence(UserPresence.State.OFFLINE, lastSeen));
            }

            @Override
            public void onGroupOnline(int gid, int count) {

            }
        });
        configuration.setTypingCallback(new TypingCallback() {
            @Override
            public void onTypingStart(int uid) {
                TypingModel.privateChatTyping(uid).change(true);
            }

            @Override
            public void onTypingEnd(int uid) {
                TypingModel.privateChatTyping(uid).change(false);
            }

            @Override
            public void onGroupTyping(int gid, int[] uids) {
                TypingModel.groupChatTyping(gid).change(uids);
            }
        });
        configuration.setEnginesFactory(new AppEngineFactory());
        configuration.setPreferencesStorage(propertiesProvider);
        configuration.setEndpoints(new Endpoints(new ConnectionEndpoint[]{
                new ConnectionEndpoint(BuildConfig.API_HOST, BuildConfig.API_PORT,
                        BuildConfig.API_SSL ? ConnectionEndpoint.Type.TCP_TLS : ConnectionEndpoint.Type.TCP)
        }));
        this.messenger = new im.actor.model.Messenger(configuration);
    }
}
