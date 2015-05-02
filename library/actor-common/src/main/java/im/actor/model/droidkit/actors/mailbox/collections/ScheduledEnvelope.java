/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.droidkit.actors.mailbox.collections;

import im.actor.model.droidkit.actors.mailbox.Envelope;

public class ScheduledEnvelope {
    private long key;
    private long time;
    private Envelope envelope;

    public ScheduledEnvelope(long key, long time, Envelope envelope) {
        this.key = key;
        this.time = time;
        this.envelope = envelope;
    }

    public long getKey() {
        return key;
    }

    public long getTime() {
        return time;
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    @Override
    public String toString() {
        return "<" + envelope.getMessage() + ">";
    }
}
