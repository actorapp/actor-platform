package im.actor.model.network.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ex3ndr on 03.09.14.
 */
public class MTUids {
    private static final AtomicLong NEXT_ID = new AtomicLong(1);

    public static long nextId() {
        return NEXT_ID.getAndIncrement();
    }
}
