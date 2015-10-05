/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods
 */
public class JavaUtil {

    /**
     * Checking if string array contains value
     *
     * @param vals  values
     * @param value value
     * @return is array contains string
     */
    public static boolean contains(String[] vals, String value) {
        for (int i = 0; i < vals.length; i++) {
            if (vals[i].equals(value)) {
                return true;
            }
        }
        return false;
    }

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

    /**
     * Getting last elements of list in reverse order
     *
     * @param elements source list
     * @param limit    maximum elements count
     * @param <T>      type of objects
     * @return result list
     */
    public static <T> List<T> last(List<T> elements, int limit) {
        ArrayList<T> res = new ArrayList<T>();
        for (int i = 0; i < elements.size(); i++) {
            if (res.size() >= limit) {
                break;
            }
            res.add(elements.get(elements.size() - 1 - i));
        }
        return res;
    }
}
