package im.actor.model.network.util;

import im.actor.model.droidkit.actors.conf.EnvConfig;
import im.actor.model.droidkit.actors.utils.AtomicLongCompat;

/**
 * Created by ex3ndr on 03.09.14.
 */
public class MTUids {
    private static final AtomicLongCompat NEXT_ID = EnvConfig.createAtomicLong(1);

    public static long nextId() {
        return NEXT_ID.getAndIncrement();
    }
}
