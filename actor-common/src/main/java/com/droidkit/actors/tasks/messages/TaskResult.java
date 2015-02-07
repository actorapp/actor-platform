package com.droidkit.actors.tasks.messages;

/**
 * Message with task result
 *
 * @param <T> type of task result
 * @author Stepan Ex3NDR Korshakov (me@ex3ndr.com)
 */
public class TaskResult<T> {
    private final int requestId;
    private final T res;

    public TaskResult(int requestId, T res) {
        this.requestId = requestId;
        this.res = res;
    }

    public T getRes() {
        return res;
    }

    public int getRequestId() {
        return requestId;
    }

    @Override
    public String toString() {
        return "TaskResult{" +
                "requestId=" + requestId +
                '}';
    }
}
