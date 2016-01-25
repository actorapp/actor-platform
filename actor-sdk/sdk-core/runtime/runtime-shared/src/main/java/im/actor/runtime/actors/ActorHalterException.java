package im.actor.runtime.actors;

public class ActorHalterException extends RuntimeException {
    public ActorHalterException() {
    }

    public ActorHalterException(String detailMessage) {
        super(detailMessage);
    }

    public ActorHalterException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ActorHalterException(Throwable throwable) {
        super(throwable);
    }
}
