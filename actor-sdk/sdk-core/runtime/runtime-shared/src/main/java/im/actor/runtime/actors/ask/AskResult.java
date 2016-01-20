package im.actor.runtime.actors.ask;

public class AskResult<T> {

    private T result;
    private long id;

    public AskResult(long id, T result) {
        this.result = result;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public T getResult() {
        return result;
    }
}
