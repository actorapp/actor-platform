package im.actor.images.loading.actors.messages;

import im.actor.images.cache.BitmapReference;

/**
 * Created by ex3ndr on 26.08.14.
 */
public class ImageLoaded {
    private final int taskId;
    private final BitmapReference reference;

    public ImageLoaded(int taskId, BitmapReference reference) {
        this.taskId = taskId;
        this.reference = reference;
    }

    public int getTaskId() {
        return taskId;
    }

    public BitmapReference getReference() {
        return reference;
    }
}
