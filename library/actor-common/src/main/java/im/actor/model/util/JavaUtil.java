package im.actor.model.util;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class JavaUtil {
    public static boolean equalsE(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a != null && b == null) {
            return false;
        }
        if (b != null && a == null) {
            return false;
        }
        return a.equals(b);
    }
}
