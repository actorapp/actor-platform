package im.actor.core.util;

public class BitMaskUtil {

    public static boolean getBitValue(long src, int index) {
        return getBitValue(src, index, false);
    }

    public static boolean getBitValue(long src, Enum e) {
        return getBitValue(src, e.ordinal(), false);
    }

    public static boolean getBitValue(long src, int index, boolean def) {
        int val = (int) ((src >> index) & 1);
        if (val == 0) {
            return def;
        } else {
            return !def;
        }
    }
}
