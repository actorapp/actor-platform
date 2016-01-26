package im.actor.runtime.actors.ask;

import im.actor.runtime.promise.PromiseResolver;

public class AskIntRequest {
    private final Object message;
    private final PromiseResolver future;

    public AskIntRequest(Object message, PromiseResolver future) {
        this.message = message;
        this.future = future;
    }

    public Object getMessage() {
        return message;
    }

    public PromiseResolver getFuture() {
        return future;
    }
}
