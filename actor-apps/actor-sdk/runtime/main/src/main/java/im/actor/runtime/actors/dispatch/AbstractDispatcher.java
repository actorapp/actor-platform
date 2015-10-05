/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors.dispatch;

/**
 * Abstract thread dispatcher for messages.
 */
public abstract class AbstractDispatcher<T, Q extends AbstractDispatchQueue<T>> {
    final private Q queue;
    final Dispatch<T> dispatch;

    protected AbstractDispatcher(Q queue, Dispatch<T> dispatch) {
        this.queue = queue;
        this.dispatch = dispatch;
        this.queue.setListener(new QueueListener() {
            @Override
            public void onQueueChanged() {
                notifyDispatcher();
            }
        });
    }

    /**
     * Queue used for dispatching
     *
     * @return queue
     */
    public Q getQueue() {
        return queue;
    }

    /**
     * Actual execution of action
     *
     * @param message action
     */
    protected void dispatchMessage(T message) {
        if (dispatch != null) {
            dispatch.dispatchMessage(message);
        }
    }

    /**
     * Notification about queue change
     */
    protected void notifyDispatcher() {

    }
}
