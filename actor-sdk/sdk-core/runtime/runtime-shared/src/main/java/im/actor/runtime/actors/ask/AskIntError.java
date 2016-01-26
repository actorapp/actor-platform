package im.actor.runtime.actors.ask;

public class AskIntError {

    private Exception exception;
    private long id;

    public AskIntError(long id, Exception exception) {
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
