package im.actor.model.droidkit.actors.utils;

/**
 * Created by ex3ndr on 09.02.15.
 */
public abstract class AtomicLongCompat {
    public abstract long get();

    public abstract long incrementAndGet();

    public abstract long getAndIncrement();

    public abstract void set(long v);
}
