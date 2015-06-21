package im.actor.images.loading;

import com.droidkit.actors.ActorRef;
import com.droidkit.actors.ActorSystem;
import com.droidkit.actors.messages.PoisonPill;
import im.actor.images.cache.BitmapReference;
import im.actor.images.loading.actors.ReceiverActor;
import im.actor.images.loading.actors.messages.ImageError;
import im.actor.images.loading.actors.messages.ImageLoaded;
import im.actor.images.loading.actors.messages.TaskCancel;
import im.actor.images.loading.actors.messages.TaskRequest;
import im.actor.images.util.UiUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ex3ndr on 16.08.14.
 */
public class ImageReceiver {

    private static final AtomicInteger RECEIVER_ID = new AtomicInteger(1);
    private static final AtomicInteger TASK_ID = new AtomicInteger(1);

    private final int id;

    private final ActorSystem actorSystem;
    private ReceiverCallback receiverCallback;

    private final ActorRef receiver;

    private int currentTask = -1;
    private BitmapReference reference;

    private String currentTaskKey;

    private ImageLoader loader;

    ImageReceiver(ImageLoader loader, ReceiverCallback receiverCallback) {
        this.id = RECEIVER_ID.incrementAndGet();
        this.loader = loader;
        this.actorSystem = loader.getActorSystem();
        this.receiver = actorSystem.actorOf(ReceiverActor.prop(this, loader), "receiver_" + id);
        this.receiverCallback = receiverCallback;
    }

    public int getId() {
        return id;
    }

    public BitmapReference getReference() {
        return reference;
    }

    private void updateReference(BitmapReference nReference) {
        if (reference != null) {
            reference.release();
            reference = null;
        }
        reference = nReference;
        receiverCallback.onImageLoaded(nReference);
    }

    public void request(AbsTask task) {
        request(task, true);
    }

    public void request(AbsTask task, boolean clearPrevious) {
        if (!UiUtil.isMainThread()) {
            throw new RuntimeException("Operations allowed only on UI thread");
        }
        if (task.getKey().equals(currentTaskKey)) {
            return;
        }
        if (clearPrevious) {
            clear();
        }

        BitmapReference cachedReference = loader.getMemoryCache().findInCache(task.getKey(), this);
        if (cachedReference != null) {
            updateReference(cachedReference);
            return;
        }

        currentTask = TASK_ID.getAndIncrement();
        currentTaskKey = task.getKey();
        receiver.send(new TaskRequest(currentTask, task));
    }

    public void clear() {
        if (!UiUtil.isMainThread()) {
            throw new RuntimeException("Operations allowed only on UI thread");
        }
        receiver.send(new TaskCancel(currentTask));
        currentTask = TASK_ID.getAndIncrement();
        receiverCallback.onImageCleared();
        if (reference != null) {
            reference.release();
            reference = null;
        }
        currentTaskKey = null;
    }

    public void close() {
        if (!UiUtil.isMainThread()) {
            throw new RuntimeException("Operations allowed only on UI thread");
        }
        clear();
        currentTask = TASK_ID.getAndIncrement();
        receiver.send(PoisonPill.INSTANCE);
        currentTaskKey = null;
        loader.destroyReceiver(this);
        receiverCallback = null;
    }

    // TODO: hide
    public void onImageLoaded(ImageLoaded loaded) {
        if (!UiUtil.isMainThread()) {
            loaded.getReference().release();
            throw new RuntimeException("Operations allowed only on UI thread");
        }

        if (loaded.getTaskId() != currentTask) {
            loaded.getReference().release();
            return;
        }

        updateReference(loaded.getReference());
    }

    // TODO: hide
    public void onImageError(ImageError error) {
        if (!UiUtil.isMainThread()) {
            throw new RuntimeException("Operations allowed only on UI thread");
        }
        if (error.getTaskId() != currentTask) {
            return;
        }
        receiverCallback.onImageError();
    }
}