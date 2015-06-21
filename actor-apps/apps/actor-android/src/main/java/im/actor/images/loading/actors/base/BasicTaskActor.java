package im.actor.images.loading.actors.base;

import android.graphics.Bitmap;

import com.droidkit.actors.tasks.TaskActor;
import im.actor.images.cache.BitmapReference;
import im.actor.images.loading.ImageLoader;
import im.actor.images.loading.AbsTask;

/**
 * Created by ex3ndr on 27.08.14.
 */
public abstract class BasicTaskActor<T extends AbsTask> extends TaskActor<BitmapReference> {

    private ImageLoader loader;
    private T task;

    public BasicTaskActor(T task, ImageLoader loader) {
        this.loader = loader;
        this.task = task;
    }

    public T getTask() {
        return task;
    }

    public ImageLoader getLoader() {
        return loader;
    }

    public abstract void startTask();

    public abstract void onTaskObsolete();

    protected void completeTask(Bitmap bitmap) {
        BitmapReference reference = loader.getMemoryCache().referenceBitmap(task.getKey(), bitmap, this);
        complete(reference);

        // All results supposed to be delivered to actors that works in UI thread
        // for now this is only ReceiverActor.
        // So, sending reference killer to ui actor will perform releasing references after
        // all notificaitons in UI
        self().send(reference);
    }

    @Override
    public void onReceive(Object message) {
        super.onReceive(message);
        if (message instanceof BitmapReference) {
            system().actorOf(ReferenceKillerActor.killer()).send(message);
        }
    }
}
