package im.actor.runtime.actors.ask;

public class AskError {

    private Exception exception;
    private long id;

    public AskError(long id, Exception exception) {
        this.exception = exception;
        this.id = id;
    }

    public Exception getException() {
        return exception;
    }

    public long getId() {
        return id;
    }
}
