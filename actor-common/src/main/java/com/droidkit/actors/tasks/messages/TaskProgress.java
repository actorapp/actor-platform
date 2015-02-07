package com.droidkit.actors.tasks.messages;

/**
 * Message with task progress
 *
 * @author Stepan Ex3NDR Korshakov (me@ex3ndr.com)
 */
public class TaskProgress {
    private final int requestId;
    private final Object progress;

    public TaskProgress(int requestId, Object progress) {
        this.requestId = requestId;
        this.progress = progress;
    }

    public int getRequestId() {
        return requestId;
    }

    public Object getProgress() {
        return progress;
    }

    @Override
    public String toString() {
        return "TaskProgress{" +
                "requestId=" + requestId +
                '}';
    }
}
