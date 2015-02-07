package com.droidkit.actors.tasks.messages;

/**
 * Message about task error
 *
 * @author Stepan Ex3NDR Korshakov (me@ex3ndr.com)
 */
public class TaskError {
    private final int requestId;
    private final Throwable throwable;

    public TaskError(int requestId, Throwable throwable) {
        this.requestId = requestId;
        this.throwable = throwable;
    }

    public int getRequestId() {
        return requestId;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public String toString() {
        return "TaskError{" +
                "requestId=" + requestId +
                ", throwable=" + throwable +
                '}';
    }
}
