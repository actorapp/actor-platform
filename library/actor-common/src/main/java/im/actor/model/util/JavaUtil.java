/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

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
    public static <T> boolean equalsE(T a, T b) {
        if (a == null && b == null) {
            return true;
        }
        if (a != null && b == null) {
            return false;
        }
        if (a == null) {
            return false;
        }
        return a.equals(b);
    }
}
