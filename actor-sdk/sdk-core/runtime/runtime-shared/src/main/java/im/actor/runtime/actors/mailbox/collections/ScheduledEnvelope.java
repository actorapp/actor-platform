/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors.mailbox.collections;

import com.google.j2objc.annotations.Property;

import im.actor.runtime.actors.mailbox.Envelope;

public class ScheduledEnvelope {
    @Property("readonly, nonatomic")
    private final long key;
    @Property("readonly, nonatomic")
    private final long time;
    @Property("readonly, nonatomic")
    private final Envelope envelope;

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
