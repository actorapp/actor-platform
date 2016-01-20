package im.actor.runtime.actors.ask;

import im.actor.runtime.actors.Future;

public class AskRequest {
    private final Object message;
    private final Future future;

    public AskRequest(Object message, Future future) {
        this.message = message;
        this.future = future;
    }

    public Object getMessage() {
        return message;
    }

    public Future getFuture() {
        return future;
    }
}
