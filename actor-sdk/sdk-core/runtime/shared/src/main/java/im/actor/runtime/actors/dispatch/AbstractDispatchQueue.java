/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors.dispatch;

/**
 * Queue for dispatching messages.
 * Implementation MUST BE thread-safe.
 */
public abstract class AbstractDispatchQueue<T> {

    /**
     * Value used for result of waitDelay when dispatcher need to wait forever
     */
    protected static final long FOREVER = 5 * 60 * 1000L;

    private QueueListener listener;

    /**
     * <p>Fetch message for dispatching and removing it from dispatch queue</p>
     * You might provide most accurate delay as you can,
     * this will minimize unnecessary thread work.
     * For example, if you will return zero here then thread will
     * loop continuously and consume processor time.
     *
     * @param time current time from ActorTime
     * @return message or delay information
     */
    public abstract DispatchResult dispatch(long time);

    /**
     * Notification about queue change.
     */
    protected void notifyQueueChanged() {
        QueueListener lListener = listener;
        if (lListener != null) {
            lListener.onQueueChanged();
        }
    }

    protected DispatchResult result(T obj) {
        return DispatchResult.result(obj);
    }

    protected DispatchResult delay(long delay) {
        return DispatchResult.delay(delay);
    }

    /**
     * Getting of current queue listener
     *
     * @return queue listener
     */
    public QueueListener getListener() {
        return listener;
    }

    /**
     * Setting queue listener
     *
     * @param listener queue listener
     */
    public void setListener(QueueListener listener) {
        this.listener = listener;
    }


}