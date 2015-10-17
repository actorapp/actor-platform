package im.actor.messenger.app.util;

import java.util.Random;

/**
 * Created by ex3ndr on 27.08.14.
 */
public class RandomUtil {
    private static final Random RANDOM = new Random();

    public static long randomId() {
        synchronized (RANDOM) {
            return Math.abs(RANDOM.nextLong());
        }
    }

    public static int randomInt() {
        synchronized (RANDOM) {
            return Math.abs(RANDOM.nextInt());
        }
    }

    public static byte[] generateSeed(int len) {
        synchronized (RANDOM) {
            byte[] res = new byte[len];
            RANDOM.nextBytes(res);
            return res;
        }
    }
}
