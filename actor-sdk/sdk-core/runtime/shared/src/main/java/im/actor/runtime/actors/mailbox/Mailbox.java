/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors.mailbox;

import im.actor.runtime.actors.mailbox.collections.EnvelopeCollection;

/**
 * Actor mailbox, queue of envelopes.
 */
public class Mailbox {

    private EnvelopeCollection envelopes;

    private final EnvelopeCollection.EnvelopeComparator comparator = new EnvelopeCollection.EnvelopeComparator() {
        @Override
        public boolean equals(Envelope a, Envelope b) {
            return isEqualEnvelope(a, b);
        }
    };

    /**
     * Creating mailbox
     *
     * @param queue MailboxesQueue
     */
    public Mailbox(MailboxesQueue queue) {
        this.envelopes = new EnvelopeCollection(queue.getEnvelopeRoot());
    }


    /**
     * Send envelope at time
     *
     * @param envelope envelope
     * @param time     time
     */
    public void schedule(Envelope envelope, long time) {
        if (envelope.getMailbox() != this) {
            throw new RuntimeException("envelope.mailbox != this mailbox");
        }

        envelopes.putEnvelope(envelope, time);
    }

    /**
     * Send envelope once at time
     *
     * @param envelope envelope
     * @param time     time
     */
    public void scheduleOnce(Envelope envelope, long time) {
        if (envelope.getMailbox() != this) {
            throw new RuntimeException("envelope.mailbox != this mailbox");
        }

        envelopes.putEnvelopeOnce(envelope, time, comparator);
    }

    /**
     * Removing of envelope from queue
     *
     * @param envelope envelope for remove
     */
    public void unschedule(Envelope envelope) {
        envelopes.removeEnvelope(envelope, comparator);
    }

    /**
     * Getting all envelopes in mailbox
     *
     * @return envelopes
     */
    public Envelope[] allEnvelopes() {
        return envelopes.allEnvelopes();
    }

    /**
     * Clearing mailbox
     */
    public void clear() {
        envelopes.clear();
    }

    /**
     * Override this if you need to change filtering for scheduleOnce behaviour.
     * By default it check equality only of class names.
     *
     * @param a
     * @param b
     * @return is equal
     */
    protected boolean isEqualEnvelope(Envelope a, Envelope b) {
        return a.getMessage().getClass() == b.getMessage().getClass();
    }

    EnvelopeCollection getEnvelopes() {
        return envelopes;
    }
}