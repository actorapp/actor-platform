package com.droidkit.actors.tasks.messages;

/**
 * Message about Task timeout
 *
 * @author Stepan Ex3NDR Korshakov (me@ex3ndr.com)
 */
public class TaskTimeout {
    private final int requestId;

    public TaskTimeout(int requestId) {
        this.requestId = requestId;
    }

    public int getRequestId() {
        return requestId;
    }
}
