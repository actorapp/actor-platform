/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.util;

import java.util.Random;

public class RandomUtils {

    private static final Random RANDOM = new Random();

    public static synchronized long nextRid() {
        return RANDOM.nextLong();
    }

    public static synchronized int randomId(int n) {
        return RANDOM.nextInt(n);
    }
}
