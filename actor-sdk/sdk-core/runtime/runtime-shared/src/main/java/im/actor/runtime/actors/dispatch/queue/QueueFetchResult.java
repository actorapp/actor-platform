package im.actor.runtime.actors.dispatch.queue;

public class QueueFetchResult<T> {

    private int id;
    private T val;

    public QueueFetchResult(int id, T val) {
        this.id = id;
        this.val = val;
    }

    public int getId() {
        return id;
    }

    public T getVal() {
        return val;
    }
}
