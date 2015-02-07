package com.droidkit.actors.mailbox.collections;

import com.droidkit.actors.mailbox.Envelope;

/**
 * Created by ex3ndr on 28.10.14.
 */
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
