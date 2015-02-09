package com.droidkit.actors.mailbox.collections;

import com.droidkit.actors.conf.EnvConfig;
import com.droidkit.actors.mailbox.Envelope;
import com.droidkit.actors.utils.AtomicIntegerCompat;
import com.droidkit.actors.utils.ThreadLocalCompat;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * Created by ex3ndr on 28.10.14.
 */
public class EnvelopeCollection {

    private static final AtomicIntegerCompat NEXT_ID = EnvConfig.createAtomicInt(1);
    private static final int DEFAULT_QUEUE_SIZE = 8;

    private final PriorityQueue<ScheduledEnvelope> envelopes = new PriorityQueue<ScheduledEnvelope>(DEFAULT_QUEUE_SIZE, new ScheduledEnvelopesComparator());

    private EnvelopeRoot root;

    private int id;

    public EnvelopeCollection(EnvelopeRoot root) {
        this.id = NEXT_ID.getAndIncrement();
        this.root = root;
        this.root.attachCollection(this);
    }

    public int getId() {
        return id;
    }

    public long getTopKey() {
        return envelopes.size() > 0 ? envelopes.peek().getKey() : 0;
    }

    public long putEnvelope(Envelope envelope, long time) {

        long key = root.buildKey(time);

        long oldKey;
        synchronized (envelopes) {
            oldKey = getTopKey();
            envelopes.offer(new ScheduledEnvelope(key, time, envelope));
        }

        if (oldKey != getTopKey()) {
            root.changedTopKey(this);
        }

        return key;
    }

    public void removeEnvelope(Envelope envelope, EnvelopeComparator comparator) {

        long oldKey;

        synchronized (envelopes) {
            oldKey = getTopKey();
            Iterator<ScheduledEnvelope> iterator = envelopes.iterator();
            while (iterator.hasNext()) {
                if (comparator.equals(iterator.next().getEnvelope(), envelope)) {
                    iterator.remove();
                }
            }
        }

        if (oldKey != envelopes.peek().getKey()) {
            root.changedTopKey(this);
        }
    }

    public long putEnvelopeOnce(Envelope envelope, long time, EnvelopeComparator comparator) {

        long key = root.buildKey(time);

        long oldKey;
        synchronized (envelopes) {
            oldKey = getTopKey();
            Iterator<ScheduledEnvelope> iterator = envelopes.iterator();
            while (iterator.hasNext()) {
                if (comparator.equals(iterator.next().getEnvelope(), envelope)) {
                    iterator.remove();
                }
            }

            envelopes.offer(new ScheduledEnvelope(key, time, envelope));
        }

        if (oldKey != getTopKey()) {
            root.changedTopKey(this);
        }

        return key;
    }

    public FetchResult fetchEnvelope(long time) {

        FetchResult result;

        long oldKey;
        synchronized (envelopes) {
            oldKey = getTopKey();
            if (envelopes.isEmpty()) {
                return null;
            }

            ScheduledEnvelope envelope = envelopes.peek();
            if (envelope.getTime() <= time) {
                envelopes.poll();
                result = FetchResult.envelope(envelope);
            } else {
                result = FetchResult.delay(envelope.getTime() - time);
            }
        }

        if (oldKey != getTopKey()) {
            root.changedTopKey(this);
        }

        return result;
    }

    public void clear() {
        synchronized (envelopes) {
            envelopes.clear();
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
            ScheduledEnvelope[] scheduledEnvelopes = envelopes.toArray(new ScheduledEnvelope[envelopes.size()]);
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

        private static ThreadLocalCompat<FetchResult> RESULT_CACHE = EnvConfig.createThreadLocal();

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

    /* package */ class ScheduledEnvelopesComparator implements Comparator<ScheduledEnvelope> {
        @Override
        public int compare(ScheduledEnvelope lop, ScheduledEnvelope rop) {
            return (int) (lop.getKey() - rop.getKey());
        }
    }
}
