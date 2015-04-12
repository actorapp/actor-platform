package im.actor.model.util;

/**
 * Utility methods
 */
public class JavaUtil {

    /**
     * Equals with null checking
     *
     * @param a first argument
     * @param b second argument
     * @return is equals result
     */
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
