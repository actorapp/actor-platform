package im.actor.core.modules.notifications;

import java.util.Collection;
import java.util.LinkedList;

public class NotificationsQueue<E> extends LinkedList<E> {
    private int limit;

    public NotificationsQueue(int limit) {
        this.limit = limit;
    }

    @Override
    public boolean add(E o) {
        super.add(o);
        while (size() > limit) {
            super.remove();
        }
        return true;
    }

    public NotificationsQueue addAllChain(Collection c) {
        super.addAll(c);
        return this;
    }
}