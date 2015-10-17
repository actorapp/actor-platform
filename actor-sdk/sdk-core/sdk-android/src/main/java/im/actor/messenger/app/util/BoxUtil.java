package im.actor.messenger.app.util;

/**
 * Created by ex3ndr on 05.10.14.
 */
public class BoxUtil {
    public static int[] unbox(Integer[] src) {
        int[] res = new int[src.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = src[i];
        }
        return res;
    }

    public static Integer[] box(int[] src) {
        Integer[] res = new Integer[src.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = src[i];
        }
        return res;
    }

    public static long[] unbox(Long[] src) {
        long[] res = new long[src.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = src[i];
        }
        return res;
    }

    public static Long[] box(long[] src) {
        Long[] res = new Long[src.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = src[i];
        }
        return res;
    }
}
