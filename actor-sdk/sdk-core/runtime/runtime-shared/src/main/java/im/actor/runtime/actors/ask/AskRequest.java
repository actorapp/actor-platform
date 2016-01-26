package im.actor.runtime.actors.ask;

import im.actor.runtime.actors.promise.PromiseExecutor;

public class AskRequest {
    private final Object message;
    private final PromiseExecutor future;

    public AskRequest(Object message, PromiseExecutor future) {
        this.message = message;
        this.future = future;
    }

    public Object getMessage() {
        return message;
    }

    public PromiseExecutor getFuture() {
        return future;
    }
}
