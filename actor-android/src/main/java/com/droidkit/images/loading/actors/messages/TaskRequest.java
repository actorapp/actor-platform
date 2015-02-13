package com.droidkit.images.loading.actors.messages;

import com.droidkit.images.loading.AbsTask;

/**
 * Created by ex3ndr on 20.08.14.
 */
public class TaskRequest<T extends AbsTask> {
    private final int requestId;
    private final T request;

    public TaskRequest(int requestId, T request) {
        this.requestId = requestId;
        this.request = request;
    }

    public int getRequestId() {
        return requestId;
    }

    public T getRequest() {
        return request;
    }
}
