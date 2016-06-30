package im.actor.runtime.actors.dispatch.queue;

import im.actor.runtime.actors.ThreadPriority;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.threading.ImmediateDispatcher;

public class QueueDispatcher<T> implements QueueCollectionListener {

    private static final int MAX_ITEMS = 10;

    private Object LOCK = new Object();
    private QueueCollection<T> collection;
    private Consumer<T> handler;
    private ImmediateDispatcher dispatcher;
    private boolean isInvalidated;
    private boolean isProcessing;

    public QueueDispatcher(String name, ThreadPriority priority, QueueCollection<T> collection,
                           Consumer<T> handler) {
        this.collection = collection;
        this.handler = handler;
        this.dispatcher = im.actor.runtime.Runtime.createImmediateDispatcher(name, priority);
        this.collection.addListener(this);
        this.onChanged();
    }

    @Override
    public void onChanged() {
        synchronized (LOCK) {
            if (isInvalidated) {
                return;
            }
            isInvalidated = true;
            if (!isProcessing) {
                dispatcher.dispatchNow(checker);
            }
        }
    }

    private Runnable checker = new Runnable() {
        @Override
        public void run() {
            synchronized (LOCK) {
                isProcessing = true;
                isInvalidated = false;
            }
            boolean isFetched = false;
            int iterations = 0;
            while (iterations < MAX_ITEMS) {
                QueueFetchResult<T> res = collection.fetch();
                if (res != null) {
                    isFetched = true;
                    try {
                        handler.apply(res.getVal());
                    } finally {
                        collection.returnQueue(res);
                    }
                } else {
                    isFetched = false;
                    break;
                }
                iterations++;
            }

            synchronized (LOCK) {
                if (isFetched || isInvalidated) {
                    dispatcher.dispatchNow(checker);
                    isInvalidated = true;
                } else {
                    isInvalidated = false;
                }
                isProcessing = false;
            }
        }
    };
}