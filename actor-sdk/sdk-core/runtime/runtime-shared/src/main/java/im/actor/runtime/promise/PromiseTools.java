package im.actor.runtime.promise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import im.actor.runtime.function.Function;

public class PromiseTools {

    public static <T> Function<List<T>, List<T>> sort(Comparator<T> comparator) {
        return ts -> {
            ArrayList<T> res = new ArrayList<>(ts);
            Collections.sort(res, comparator);
            return res;
        };
    }

    public static <T extends Comparable<T>> Function<List<T>, List<T>> sort() {
        return ts -> {
            ArrayList<T> res = new ArrayList<>(ts);
            Collections.sort(res);
            return res;
        };
    }
}
