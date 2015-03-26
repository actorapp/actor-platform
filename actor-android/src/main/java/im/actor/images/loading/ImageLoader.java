package im.actor.images.loading;

import android.content.Context;

import com.droidkit.actors.ActorSystem;
import com.droidkit.actors.android.UiActorDispatcher;
import im.actor.images.cache.BitmapClasificator;
import im.actor.images.cache.DiskCache;
import im.actor.images.cache.MemoryCache;
import im.actor.images.loading.actors.FilePreviewActor;
import im.actor.images.loading.actors.RawFileActor;
import im.actor.images.loading.actors.PreviewContentActor;
import im.actor.images.loading.tasks.PreviewContentTask;
import im.actor.images.loading.tasks.PreviewFileTask;
import im.actor.images.loading.tasks.RawFileTask;

import java.util.HashSet;

/**
 * Created by ex3ndr on 16.08.14.
 */
public class ImageLoader {
    private ActorSystem actorSystem;

    private HashSet<ImageReceiver> receivers = new HashSet<ImageReceiver>();
    private MemoryCache memoryCache;
    private TaskResolver taskResolver;
    private DiskCache internalDiskCache;
    private Context context;

    public ImageLoader(BitmapClasificator clasificator, Context context) {
        this.context = context;
        this.actorSystem = new ActorSystem();
        this.actorSystem.addDispatcher("ui", new UiActorDispatcher("ui", actorSystem));
        this.taskResolver = new TaskResolver(this);
        this.memoryCache = new MemoryCache(clasificator);
        this.internalDiskCache = new DiskCache(context.getFilesDir().getAbsolutePath() + "dcache/");
        initDefaultResolver();
    }

    private void initDefaultResolver() {
        taskResolver.register(RawFileTask.class, RawFileActor.class);
        taskResolver.register(PreviewContentTask.class, PreviewContentActor.class);
        taskResolver.register(PreviewFileTask.class, FilePreviewActor.class);
    }

    public Context getContext() {
        return context;
    }

    public MemoryCache getMemoryCache() {
        return memoryCache;
    }

    public DiskCache getInternalDiskCache() {
        return internalDiskCache;
    }

    public TaskResolver getTaskResolver() {
        return taskResolver;
    }

    public ActorSystem getActorSystem() {
        return actorSystem;
    }

    public ImageReceiver createReceiver(ReceiverCallback callback) {
        ImageReceiver res = new ImageReceiver(this, callback);
        receivers.add(res);
        return res;
    }

    void destroyReceiver(ImageReceiver receiver) {
        receivers.remove(receiver);
    }
}