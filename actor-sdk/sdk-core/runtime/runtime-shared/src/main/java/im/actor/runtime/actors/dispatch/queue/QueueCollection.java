package im.actor.runtime.actors.dispatch.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import im.actor.runtime.threading.AtomicIntegerCompat;

public class QueueCollection<T> {

    private AtomicIntegerCompat NEXT_ID = im.actor.runtime.Runtime.createAtomicInt(0);

    private HashMap<Integer, Queue<T>> queues = new HashMap<>();
    private LinkedList<Queue<T>> pending = new LinkedList<>();
    private ArrayList<QueueCollectionListener> listeners = new ArrayList<>();

    public synchronized void addListener(QueueCollectionListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void removeListener(QueueCollectionListener listener) {
        listeners.remove(listener);
    }

    public synchronized int spawnQueue() {
        int id = NEXT_ID.getAndIncrement();
        queues.put(id, new Queue<T>(id));
        return id;
    }

    public synchronized void disposeQueue(int id) {
        Queue<T> q = queues.remove(id);
        if (q != null) {
            pending.remove(q);
        }
    }

    public synchronized void post(int id, T value) {
        post(id, value, false);
    }

    public synchronized void post(int id, T value, boolean isFirst) {
        Queue<T> queue = queues.get(id);
        if (queue == null) {
            return;
        }
        boolean wasEmptyPending = pending.isEmpty();
        boolean wasEmpty = queue.getQueue().isEmpty();

        if (isFirst) {
            queue.getQueue().add(0, value);
        } else {
            queue.getQueue().add(value);
        }

        if (wasEmpty && !queue.isLocked()) {
            pending.add(queue);
        }
        if (wasEmptyPending) {
            for (QueueCollectionListener l : listeners) {
                l.onChanged();
            }
        }
    }

    public synchronized QueueFetchResult<T> fetch() {
        if (pending.isEmpty()) {
            return null;
        }
        Queue<T> queue = pending.remove(0);
        queue.setIsLocked(true);
        T val = queue.getQueue().remove(0);
        return new QueueFetchResult<>(queue.getId(), val);
    }

    public synchronized void returnQueue(QueueFetchResult<T> res) {
        Queue<T> queue = queues.get(res.getId());
        if (queue == null) {
            return;
        }

        queue.setIsLocked(false);

        if (!queue.getQueue().isEmpty()) {
            boolean wasEmptyPending = pending.isEmpty();
            pending.add(queue);
            if (wasEmptyPending) {
                for (QueueCollectionListener l : listeners) {
                    l.onChanged();
                }
            }
        }
    }

    public synchronized List<T> getAllPending(int id) {
        Queue<T> queue = queues.get(id);
        if (queue == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(queue.getQueue());
    }
}
