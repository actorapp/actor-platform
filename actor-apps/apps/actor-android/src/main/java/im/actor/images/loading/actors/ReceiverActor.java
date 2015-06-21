package im.actor.images.loading.actors;

import com.droidkit.actors.*;
import com.droidkit.actors.tasks.AskCallback;
import com.droidkit.actors.tasks.AskFuture;
import im.actor.images.cache.BitmapReference;
import im.actor.images.loading.ImageLoader;
import im.actor.images.loading.ImageReceiver;
import im.actor.images.loading.actors.messages.*;
import im.actor.images.loading.TaskResolver;

import java.lang.ref.WeakReference;

import im.actor.messenger.app.util.Logger;

/**
 * Created by ex3ndr on 20.08.14.
 */
public final class ReceiverActor extends Actor {

    public static Props<ReceiverActor> prop(final ImageReceiver receiver, final ImageLoader imageLoader) {
        return Props.create(ReceiverActor.class, new ActorCreator<ReceiverActor>() {
            @Override
            public ReceiverActor create() {
                return new ReceiverActor(receiver, imageLoader);
            }
        }).changeDispatcher("ui");
    }

    private int taskId = -1;
    private AskFuture future;
    private TaskResolver resolver;
    private WeakReference<ImageReceiver> receiver;
    private ImageLoader loader;

    public ReceiverActor(ImageReceiver receiver, ImageLoader imageLoader) {
        this.loader = imageLoader;
        this.resolver = imageLoader.getTaskResolver();
        this.receiver = new WeakReference<ImageReceiver>(receiver);
    }

    @Override
    public void onReceive(Object message) {
        super.onReceive(message);

        final String path = self().getPath();

        if (message instanceof TaskRequest) {
            final TaskRequest taskRequest = (TaskRequest) message;
            taskId = taskRequest.getRequestId();
            final int currentId = taskId;

            Logger.d("ImageReceiver", path + "|RequestTask " + taskId);
            // Cancel current work
            cancel();

            BitmapReference reference = loader.getMemoryCache().findInCache(taskRequest.getRequest().getKey(), this);
            if (reference != null) {
                Logger.d("ImageReceiver", path + "|Founded in cache");
                ImageReceiver r = receiver.get();
                if (r == null) {
                    Logger.d("ImageReceiver", path + "|empty receiver");
                    context().stopSelf();
                    return;
                }
                r.onImageLoaded(new ImageLoaded(currentId, reference));
                return;
            }

            try {
                final ActorSelection selection = resolver.resolveSelection(taskRequest.getRequest());
                future = ask(selection, new AskCallback<BitmapReference>() {
                    @Override
                    public void onResult(BitmapReference result) {
                        Logger.d("ImageReceiver", path + "|Work result");
                        ImageReceiver r = receiver.get();
                        if (r == null) {
                            Logger.d("ImageReceiver", path + "|empty receiver");
                            context().stopSelf();
                            return;
                        }
                        r.onImageLoaded(new ImageLoaded(currentId, result.fork(receiver)));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Logger.d("ImageReceiver", path + "|Work error: " + throwable);
                        ImageReceiver r = receiver.get();
                        if (r == null) {
                            Logger.d("ImageReceiver", path + "|empty receiver");
                            context().stopSelf();
                            return;
                        }
                        r.onImageError(new ImageError(currentId, throwable));
                    }
                });
                Logger.d("ImageReceiver", path + "|Requested new work @" + selection.getPath());
            } catch (Exception e) {
                Logger.d("ImageReceiver", path + "|Error during resolve");
                ImageReceiver r = receiver.get();
                if (r == null) {
                    Logger.d("ImageReceiver", path + "|empty receiver");
                    context().stopSelf();
                    return;
                }
                r.onImageError(new ImageError(currentId, e));
            }
        } else if (message instanceof TaskCancel) {
            TaskCancel taskCancel = (TaskCancel) message;
            if (taskId == taskCancel.getRequestId()) {
                cancel();
            }
        }
    }

    private void cancel() {
        if (future != null) {
            future.cancel();
            future = null;
        }
    }
}