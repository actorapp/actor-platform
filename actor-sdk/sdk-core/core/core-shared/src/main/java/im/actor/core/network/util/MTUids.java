/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.util;


import im.actor.runtime.threading.AtomicLongCompat;

public class MTUids {

    private static final AtomicLongCompat NEXT_ID = im.actor.runtime.Runtime.createAtomicLong(1);

    public static long nextId() {
        return NEXT_ID.getAndIncrement();
    }
}
