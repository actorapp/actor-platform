package im.actor.model.droidkit.bser;

import java.util.Arrays;

/**
 * Created by ex3ndr on 01.03.15.
 */
public class Utils {
    public static String byteArrayToString(byte[] data) {
        if (data == null) {
            return "null";
        } else {
            return Arrays.toString(data);
        }
    }

    public static String byteArrayToStringCompact(byte[] data) {
        if (data == null) {
            return "null";
        } else {
            return Arrays.toString(data);
        }
    }
}
