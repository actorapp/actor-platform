package im.actor.images.loading.actors.messages;

/**
 * Created by ex3ndr on 26.08.14.
 */
public class ImageError {
    private final int taskId;
    private final Throwable exception;

    public ImageError(int taskId, Throwable exception) {
        this.taskId = taskId;
        this.exception = exception;
    }

    public int getTaskId() {
        return taskId;
    }

    public Throwable getException() {
        return exception;
    }
}
