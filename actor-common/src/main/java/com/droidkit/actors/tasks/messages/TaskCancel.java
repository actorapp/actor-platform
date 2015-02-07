package com.droidkit.actors.tasks.messages;

/**
 * Message about task cancelling
 *
 * @author Stepan Ex3NDR Korshakov (me@ex3ndr.com)
 */
public class TaskCancel {
    private int requestId;

    public TaskCancel(int requestId) {
        this.requestId = requestId;
    }

    public int getRequestId() {
        return requestId;
    }

    @Override
    public String toString() {
        return "TaskCancel{" +
                "requestId=" + requestId +
                '}';
    }
}
