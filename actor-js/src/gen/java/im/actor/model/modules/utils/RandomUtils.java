package im.actor.model.modules.utils;

import java.util.Random;

/**
 * Created by ex3ndr on 11.02.15.
 */
public class RandomUtils {

    private static final Random RANDOM = new Random();

    public static synchronized long nextRid() {
        return RANDOM.nextLong();
    }
}
