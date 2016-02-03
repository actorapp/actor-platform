package im.actor.runtime.util;

import java.util.Collection;

public abstract class Utils {
    public static String toString(Collection collection) {
        String res = "[";
        for (Object o : collection) {
            if (res.length() > 1) {
                res += ", ";
            }
            res += o;
        }
        return res + "]";
    }
}
