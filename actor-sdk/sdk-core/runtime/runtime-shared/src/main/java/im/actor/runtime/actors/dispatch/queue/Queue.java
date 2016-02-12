package im.actor.runtime.actors.dispatch.queue;

import java.util.LinkedList;

public class Queue<T> {

    private final int id;
    private final LinkedList<T> queue;
    private boolean isLocked;

    public Queue(int id) {
        this.id = id;
        this.queue = new LinkedList<>();
        this.isLocked = false;
    }

    public int getId() {
        return id;
    }

    public LinkedList<T> getQueue() {
        return queue;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setIsLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }
}
