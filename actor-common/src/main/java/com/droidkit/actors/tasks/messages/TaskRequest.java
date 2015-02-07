package com.droidkit.actors.tasks.messages;

/**
 * Message about requesting task
 *
 * @author Stepan Ex3NDR Korshakov (me@ex3ndr.com)
 */
public class TaskRequest {
    private final int requestId;

    public TaskRequest(int requestId) {
        this.requestId = requestId;
    }

    public int getRequestId() {
        return requestId;
    }

    @Override
    public String toString() {
        return "TaskRequest{" +
                "requestId=" + requestId +
                '}';
    }
}
