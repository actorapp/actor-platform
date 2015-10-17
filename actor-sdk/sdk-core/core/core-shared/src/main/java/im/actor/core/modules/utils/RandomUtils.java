/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.utils;

import java.util.Random;

public class RandomUtils {

    private static final Random RANDOM = new Random();

    public static synchronized long nextRid() {
        return RANDOM.nextLong();
    }
}
