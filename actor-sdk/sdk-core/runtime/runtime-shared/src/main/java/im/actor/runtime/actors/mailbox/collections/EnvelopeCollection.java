/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors.mailbox.collections;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import im.actor.runtime.actors.mailbox.Envelope;
import im.actor.runtime.threading.AtomicIntegerCompat;
import im.actor.runtime.threading.ThreadLocalCompat;

public class EnvelopeCollection {

    private static final AtomicIntegerCompat NEXT_ID = im.actor.runtime.Runtime.createAtomicInt(1);

    private final TreeMap<Long, ScheduledEnvelope> envelopes = new TreeMap<Long, ScheduledEnvelope>();

    private EnvelopeRoot root;

    private int id;

    private long topKey;

    public EnvelopeCollection(EnvelopeRoot root) {
        this.id = NEXT_ID.getAndIncrement();
        this.root = root;
        this.topKey = 0L;
        this.root.attachCollection(this);
    }

    public int getId() {
        return id;
    }

    public long getTopKey() {
        return topKey;
    }

    public long putEnvelope(Envelope envelope, long time) {

        long key = root.buildKey(time);

        long oldKey;
        synchronized (envelopes) {
            oldKey = topKey;
            envelopes.put(key, new ScheduledEnvelope(key, time, envelope));

            if (key < topKey || topKey == 0) {
                topKey = key;
            }
        }

        if (oldKey != topKey) {
            root.changedTopKey(this);
        }

        return key;
    }

    public void removeEnvelope(Envelope envelope, EnvelopeComparator comparator) {

        long oldKey;

        synchronized (envelopes) {
            oldKey = topKey;
            Iterator<Map.Entry<Long, ScheduledEnvelope>> iterator = envelopes.entrySet().iterator();
            while (iterator.hasNext()) {
                if (comparator.equals(iterator.next().getValue().getEnvelope(), envelope)) {
                    iterator.remove();
                }
            }

            if (envelopes.isEmpty()) {
                topKey = 0;
            } else {
                topKey = envelopes.firstKey();
            }
        }

        if (oldKey != topKey) {
            root.changedTopKey(this);
        }
    }

    public long putEnvelopeOnce(Envelope envelope, long time, EnvelopeComparator comparator) {

        long key = root.buildKey(time);

        long oldKey;
        synchronized (envelopes) {
            oldKey = topKey;
            Iterator<Map.Entry<Long, ScheduledEnvelope>> iterator = envelopes.entrySet().iterator();
            while (iterator.hasNext()) {
                if (comparator.equals(iterator.next().getValue().getEnvelope(), envelope)) {
                    iterator.remove();
                }
            }

            envelopes.put(key, new ScheduledEnvelope(key, time, envelope));
            topKey = envelopes.firstKey();
        }

        if (oldKey != topKey) {
            root.changedTopKey(this);
        }

        return key;
    }

    public FetchResult fetchEnvelope(long time) {

        FetchResult result;

        long oldKey;
        synchronized (envelopes) {
            oldKey = topKey;
            if (envelopes.isEmpty()) {
                return null;
            }

            ScheduledEnvelope envelope = envelopes.firstEntry().getValue();
            if (envelope.getTime() <= time) {
                envelopes.remove(envelope.getKey());

                if (envelopes.isEmpty()) {
                    topKey = 0;
                } else {
                    topKey = envelopes.firstKey();
                }
                result = FetchResult.envelope(envelope);
            } else {
                result = FetchResult.delay(envelope.getTime() - time);
            }
        }

        if (oldKey != topKey) {
            root.changedTopKey(this);
        }

        return result;
    }

    public void clear() {
        synchronized (envelopes) {
            envelopes.clear();
            topKey = 0;
        }
        root.changedTopKey(this);
    }

    public int getSize() {
        synchronized (envelopes) {
            return envelopes.size();
        }
    }

    public Envelope[] allEnvelopes() {
        synchronized (envelopes) {
            ScheduledEnvelope[] scheduledEnvelopes = envelopes.values().toArray(new ScheduledEnvelope[envelopes.size()]);
            Envelope[] res = new Envelope[scheduledEnvelopes.length];
            for (int i = 0; i < res.length; i++) {
                res[i] = scheduledEnvelopes[i].getEnvelope();
            }
            return res;
        }
    }

    public interface EnvelopeComparator {
        public boolean equals(Envelope a, Envelope b);
    }

    public static class FetchResult {

        private static ThreadLocalCompat<FetchResult> RESULT_CACHE = im.actor.runtime.Runtime.createThreadLocal();

        public static FetchResult envelope(ScheduledEnvelope envelope) {
            FetchResult res = RESULT_CACHE.get();
            if (res != null) {
                RESULT_CACHE.remove();
                res.update(envelope, 0);
            } else {
                res = new FetchResult(envelope);
            }

            return res;
        }

        public static FetchResult delay(long delay) {
            FetchResult res = RESULT_CACHE.get();
            if (res != null) {
                RESULT_CACHE.remove();
                res.update(null, delay);
            } else {
                res = new FetchResult(delay);
            }

            return res;
        }

        private ScheduledEnvelope envelope;
        private long delay;

        private FetchResult(ScheduledEnvelope envelope) {
            this.envelope = envelope;
        }

        private FetchResult(long delay) {
            this.delay = delay;
        }

        public ScheduledEnvelope getEnvelope() {
            return envelope;
        }

        public long getDelay() {
            return delay;
        }

        public void update(ScheduledEnvelope envelope, long delay) {
            this.envelope = envelope;
            this.delay = delay;
        }

        public void recycle() {
            RESULT_CACHE.set(this);
        }
    }
}
