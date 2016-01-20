package im.actor.runtime.actors.ask;

import im.actor.runtime.actors.Future;

public class AskRequest<T> {
    private final Object message;
    private final Future<T> future;

    public AskRequest(Object message, Future<T> future) {
        this.message = message;
        this.future = future;
    }

    public Object getMessage() {
        return message;
    }

    public Future<T> getFuture() {
        return future;
    }
}
