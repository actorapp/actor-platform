/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network.util;

import im.actor.model.droidkit.actors.Environment;
import im.actor.model.util.AtomicLongCompat;

public class MTUids {
    private static final AtomicLongCompat NEXT_ID = Environment.createAtomicLong(1);

    public static long nextId() {
        return NEXT_ID.getAndIncrement();
    }
}
