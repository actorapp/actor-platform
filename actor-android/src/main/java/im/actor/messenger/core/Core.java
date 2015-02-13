package im.actor.messenger.core;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.droidkit.actors.android.AndroidTrace;

import im.actor.api.ActorApi;
import im.actor.api.ActorApiCallback;
import im.actor.api.ActorApiConfig;
import im.actor.api.mtp.MTProtoEndpoint;
import im.actor.api.scheme.ApiRequests;
import im.actor.messenger.BuildConfig;
import im.actor.messenger.api.AndroidApiStorage;
import im.actor.messenger.app.emoji.EmojiProcessor;
import im.actor.messenger.app.view.Formatter;
import im.actor.messenger.core.actors.api.ApiLogger;
import im.actor.messenger.core.actors.chat.DialogsHistoryActor;
import im.actor.messenger.core.actors.contacts.BookImportActor;
import im.actor.messenger.core.actors.contacts.ContactsActor;
import im.actor.messenger.core.actors.messages.ClearChatActor;
import im.actor.messenger.core.actors.messages.DeleteMessagesActor;
import im.actor.messenger.core.actors.messages.PlainReadActor;
import im.actor.messenger.core.actors.messages.PlainReceivedActor;
import im.actor.messenger.core.actors.messages.ReadEncryptedActor;
import im.actor.messenger.core.actors.messages.ReceivedEnctyptedActor;
import im.actor.messenger.core.actors.presence.MyPresenceActor;
import im.actor.messenger.model.ProfileSyncState;
import im.actor.messenger.storage.AccountKeyStorage;
import im.actor.messenger.storage.DbProvider;

import com.droidkit.actors.android.UiActorDispatcher;
import com.droidkit.actors.typed.TypedCreator;
import com.droidkit.engine.event.NotificationCenter;
import com.droidkit.engine.event.StateInitValue;
import com.droidkit.images.cache.BitmapClasificator;
import com.droidkit.images.loading.ImageLoader;

import im.actor.messenger.core.actors.push.GooglePushActor;
import im.actor.messenger.core.actors.api.ApiStateBroker;
import im.actor.messenger.core.actors.send.MediaSenderActor;
import im.actor.messenger.core.images.*;
import im.actor.messenger.core.actors.api.SequenceActor;
import im.actor.messenger.core.actors.send.MessageSendActor;
import im.actor.messenger.core.auth.AuthModel;
import im.actor.messenger.api.NetworkState;
import im.actor.messenger.storage.AuthStorage;
import im.actor.messenger.util.Logger;

import static com.droidkit.actors.ActorSystem.system;

/**
 * Created by ex3ndr on 30.08.14.
 */
public class Core {

    public static final int AUTH_STATE = 0x04;

    private static volatile Core core;

    public static void init(Application application) {
        long start = System.currentTimeMillis();
        AppContext.setContext(application);
        DbProvider.getDatabase(application);
        Formatter.init(application);

        Log.d("ACTOR_INIT", "phase1 in " + (System.currentTimeMillis() - start) + " ms");
        start = System.currentTimeMillis();

        core = new Core(application);

        Log.d("ACTOR_INIT", "phase2 in " + (System.currentTimeMillis() - start) + " ms");
        start = System.currentTimeMillis();

        core.start();

        Log.d("ACTOR_INIT", "phase3 in " + (System.currentTimeMillis() - start) + " ms");
    }

    public static Core core() {
        if (core == null) {
            throw new RuntimeException("Core is not initialized");
        }

        return core;
    }

    public static EmojiProcessor emoji() {
        return core().getEmojiProcessor();
    }

    public static AuthModel auth() {
        return core().getAuthModel();
    }

    public static ApiRequests requests() {
        return core().actorApi.getRequests();
    }

    public ActorApi getActorApi() {
        return actorApi;
    }

    public static boolean isLoggedIn() {
        return core().authStorage.isLoggedIn();
    }

    public static int myUid() {
        return core().authStorage.getUid();
    }

    public static AccountKeyStorage keyStorage() {
        return core().keyStorage;
    }

    private AuthStorage authStorage;
    private AuthModel authModel;
    private ImageLoader imageLoader;
    private EmojiProcessor emojiProcessor;
    private AccountKeyStorage keyStorage;

    private ActorApi actorApi;

    private Core(Application application) {

        long start = System.currentTimeMillis();

        this.authStorage = new AuthStorage(application);
        this.keyStorage = new AccountKeyStorage(application);

        Log.d("ACTOR_INIT", "phase1_0 in " + (System.currentTimeMillis() - start) + " ms");
        start = System.currentTimeMillis();

        this.authModel = new AuthModel(application, authStorage);

        Log.d("ACTOR_INIT", "phase1_1 in " + (System.currentTimeMillis() - start) + " ms");
        start = System.currentTimeMillis();

        this.emojiProcessor = new EmojiProcessor(application);

        Log.d("ACTOR_INIT", "phase1_2 in " + (System.currentTimeMillis() - start) + " ms");
        start = System.currentTimeMillis();

        ActivityManager activityManager = (ActivityManager) AppContext.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        int memoryInMB = Math.min(activityManager.getMemoryClass(), 32);
        long totalAppHeap = memoryInMB * 1024 * 1024;
        int cacheLimit = (int) totalAppHeap / 4;
        int freeCacheLimit = cacheLimit / 2;

        Log.d("ACTOR_INIT", "Init image clasificator for " + cacheLimit / (1024 * 1024) + " mb");

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

        Log.d("ACTOR_INIT", "phase1_3 in " + (System.currentTimeMillis() - start) + " ms");
        start = System.currentTimeMillis();

        AndroidTrace.initTrace(system(), new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                if (BuildConfig.REPORT_CRASHES) {
                    // Crashlytics.logException(ex);
                }
            }
        });

        Log.d("ACTOR_INIT", "phase1_4 in " + (System.currentTimeMillis() - start) + " ms");
        start = System.currentTimeMillis();

        system().setClassLoader(AppContext.getContext().getClassLoader());

        Log.d("ACTOR_INIT", "phase1_5 in " + (System.currentTimeMillis() - start) + " ms");
        start = System.currentTimeMillis();

        system().addDispatcher("ui", new UiActorDispatcher("ui", system()));
        system().addDispatcher("db", 1);
        system().addDispatcher("contacts", 1);
        system().addDispatcher("file_encryption", 1);
        system().addDispatcher("rsa", 1);
        system().addDispatcher("updates", 1);
        system().addDispatcher("push", 1);
        system().addDispatcher("opus", 1);

        Log.d("ACTOR_INIT", "phase1_6 in " + (System.currentTimeMillis() - start) + " ms");
        start = System.currentTimeMillis();
    }

    public void start() {
        try {
            long start = System.currentTimeMillis();

            Log.d("ACTOR_INIT", "phase2_0 in " + (System.currentTimeMillis() - start) + " ms");
            start = System.currentTimeMillis();

            AndroidApiStorage apiStorage = new AndroidApiStorage(AppContext.getContext());
            ActorApiCallback callback = TypedCreator.typed(system().actorOf(ApiStateBroker.class, "api_state"),
                    ActorApiCallback.class);
            ApiLogger apiLogger = new ApiLogger();
            MTProtoEndpoint[] endpoints = new MTProtoEndpoint[]{
                    new MTProtoEndpoint(BuildConfig.API_SSL ? MTProtoEndpoint.EndpointType.TLS_TCP :
                            MTProtoEndpoint.EndpointType.PLAIN_TCP, BuildConfig.API_HOST, BuildConfig.API_PORT)
            };

            ActorApiConfig.Builder builder = new ActorApiConfig.Builder()
                    .setStorage(apiStorage)
                    .setEndpoints(endpoints)
                    .setLog(apiLogger)
                    .setApiCallback(callback)
                    .setChromeSupportEnabled(BuildConfig.ENABLE_CHROME);

            actorApi = new ActorApi(builder.build());

            Log.d("ACTOR_INIT", "phase2_1 in " + (System.currentTimeMillis() - start) + " ms");
            start = System.currentTimeMillis();

            ProfileSyncState.init(AppContext.getContext());

            if (authModel.isAuthorized()) {

                // Sequence
                system().actorOf(SequenceActor.sequence()).send(new SequenceActor.Invalidate());

                // Contacts
                system().actorOf(BookImportActor.contactsImport()).send(new BookImportActor.StartSync());
                ContactsActor.contactsList();

                // Read/Receive states
                system().actorOf(ReadEncryptedActor.messageReader());
                system().actorOf(ReceivedEnctyptedActor.messageReceiver());
                PlainReceivedActor.plainReceive();
                PlainReadActor.plainRead();

                // Deletion of messages and chats
                system().actorOf(DeleteMessagesActor.messageReader());
                system().actorOf(ClearChatActor.clearChat());

                // Message and Media sending
                MessageSendActor.messageSender();
                MediaSenderActor.mediaSender();

                // History loading
                // DialogsHistoryActor.get().onAuthenticated();
            }

            Log.d("ACTOR_INIT", "phase2_2 in " + (System.currentTimeMillis() - start) + " ms");
            start = System.currentTimeMillis();

            GooglePushActor.push();

            NetworkState.getInstance();

            Log.d("ACTOR_INIT", "phase2_3 in " + (System.currentTimeMillis() - start) + " ms");
            start = System.currentTimeMillis();

            NotificationCenter.getInstance().registerState(AUTH_STATE, new StateInitValue() {
                @Override
                public Object[] initState(int type, long id) {
                    if (authModel.isAuthorized()) {
                        return new Object[]{AuthModel.AuthProcessState.STATE_SIGNED};
                    } else {
                        return new Object[]{authModel.getAuthProcessState().getStateId()};
                    }
                }
            });

            Log.d("ACTOR_INIT", "phase2_4 in " + (System.currentTimeMillis() - start) + " ms");
            start = System.currentTimeMillis();
        } catch (Throwable t) {
            Logger.d(t);
        }
    }

    public EmojiProcessor getEmojiProcessor() {
        return emojiProcessor;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public AuthModel getAuthModel() {
        return authModel;
    }

    public void afterLogIn() {
        system().actorOf(BookImportActor.contactsImport()).send(new BookImportActor.StartSync());
        ContactsActor.contactsList();
        system().actorOf(SequenceActor.sequence()).send(new SequenceActor.Invalidate());
        MyPresenceActor.myPresence().send(new MyPresenceActor.PerformPresence());
        // DialogsHistoryActor.get().onAuthenticated();
    }
}
