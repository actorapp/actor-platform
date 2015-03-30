package im.actor.model.util;

import java.util.Random;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class RandomUtils {
    private static Random random = new Random();

    public static synchronized byte[] seed(int size) {
        byte[] res = new byte[size];
        random.nextBytes(res);
        return res;
    }
}
