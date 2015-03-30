package im.actor.model.network.util;

import im.actor.model.droidkit.actors.Environment;
import im.actor.model.util.AtomicLongCompat;

/**
 * Created by ex3ndr on 03.09.14.
 */
public class MTUids {
    private static final AtomicLongCompat NEXT_ID = Environment.createAtomicLong(1);

    public static long nextId() {
        return NEXT_ID.getAndIncrement();
    }
}
