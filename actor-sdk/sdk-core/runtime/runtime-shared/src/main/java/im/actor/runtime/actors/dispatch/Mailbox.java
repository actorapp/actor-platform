/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors.dispatch;

import im.actor.runtime.actors.dispatch.queue.QueueCollection;

/**
 * Actor mailbox, queue of envelopes.
 */
public class Mailbox {

    private final QueueCollection<Envelope> queueCollection;
    private final int queueId;

    /**
     * Creating mailbox
     *
     * @param queueCollection Queue Collection of envelopes
     */
    public Mailbox(QueueCollection<Envelope> queueCollection) {
        this.queueCollection = queueCollection;
        this.queueId = queueCollection.spawnQueue();
    }


    /**
     * Send envelope at time
     *
     * @param envelope envelope
     */
    public void schedule(Envelope envelope) {
        if (envelope.getMailbox() != this) {
            throw new RuntimeException("envelope.mailbox != this mailbox");
        }

        queueCollection.post(queueId, envelope);
    }

    /**
     * Send envelope first
     *
     * @param envelope envelope
     */
    public void scheduleFirst(Envelope envelope) {
        if (envelope.getMailbox() != this) {
            throw new RuntimeException("envelope.mailbox != this mailbox");
        }

        queueCollection.post(queueId, envelope, true);
    }


    public Envelope[] dispose() {
        Envelope[] res = queueCollection.getAllPending(queueId).toArray(new Envelope[0]);
        queueCollection.disposeQueue(queueId);
        return res;
    }
}