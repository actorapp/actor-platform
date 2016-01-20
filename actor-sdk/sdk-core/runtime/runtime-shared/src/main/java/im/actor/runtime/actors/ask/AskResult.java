package im.actor.runtime.actors.ask;

public class AskResult {

    private Object result;
    private long id;

    public AskResult(long id, Object result) {
        this.result = result;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public Object getResult() {
        return result;
    }
}
