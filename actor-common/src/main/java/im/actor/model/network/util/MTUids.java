package im.actor.model.network.util;

import com.droidkit.actors.conf.EnvConfig;
import com.droidkit.actors.utils.AtomicLongCompat;

/**
 * Created by ex3ndr on 03.09.14.
 */
public class MTUids {
    private static final AtomicLongCompat NEXT_ID = EnvConfig.createAtomicLong(1);

    public static long nextId() {
        return NEXT_ID.getAndIncrement();
    }
}
