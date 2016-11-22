package im.actor.runtime.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import im.actor.runtime.function.Function;
import im.actor.runtime.function.Predicate;
import im.actor.runtime.function.Supplier;
import im.actor.runtime.promise.Promise;

public class ManagedList<T> extends ArrayList<T> {

    public static <T> ManagedList<T> of(Collection<? extends T> collection) {
        return new ManagedList<>(collection);
    }

    public static <T> ManagedList<T> empty() {
        return new ManagedList<>();
    }

    public static <T> ManagedList<T> of(T... vals) {
        ManagedList<T> res = new ManagedList<>();
        for (T t : vals) {
            res.add(t);
        }
        return res;
    }

    public static <T> ManagedList<T> of(Supplier<T> supplier, int count) {
        ManagedList<T> res = new ManagedList<>();
        for (int i = 0; i < count; i++) {
            res.add(supplier.get());
        }
        return res;
    }


    private ManagedList(int capacity) {
        super(capacity);
    }

    private ManagedList() {
    }

    private ManagedList(Collection<? extends T> collection) {
        super(collection);
    }

    public ManagedList<T> filter(Predicate<T> predicate) {
        ManagedList<T> res = new ManagedList<>();
        for (T t : this) {
            if (predicate.apply(t)) {
                res.add(t);
            }
        }
        return res;
    }

    public boolean isAll(Predicate<T> predicate) {
        for (T t : this) {
            if (!predicate.apply(t)) {
                return false;
            }
        }
        return true;
    }

    public boolean isAny(Predicate<T> predicate) {
        for (T t : this) {
            if (predicate.apply(t)) {
                return true;
            }
        }
        return false;
    }

    public <R> ManagedList<R> map(Function<T, R> map) {
        ManagedList<R> res = new ManagedList<>();
        for (T t : this) {
            res.add(map.apply(t));
        }
        return res;
    }

    public <R> ManagedList<R> flatMap(Function<T, R[]> map) {
        ManagedList<R> res = new ManagedList<>();
        for (T t : this) {
            R[] mapR = map.apply(t);
            for (R r : mapR) {
                res.add(r);
            }
        }
        return res;
    }

    public <T> ManagedList<T> generate(Supplier<T> supplier, int count) {
        ManagedList<T> res = new ManagedList<>();
        for (int i = 0; i < count; i++) {
            res.add(supplier.get());
        }
        return res;
    }

    public ManagedList<T> sorted(Comparator<T> comparator) {
        ManagedList<T> res = new ManagedList<>(this);
        Collections.sort(res, comparator);
        return res;
    }

    public T firstOrNull(Predicate<T> predicate) {
        for (T t : this) {
            if (predicate.apply(t)) {
                return t;
            }
        }
        return null;
    }

    public T first(Predicate<T> predicate) {
        for (T t : this) {
            if (predicate.apply(t)) {
                return t;
            }
        }
        throw new RuntimeException("Unable to find element");
    }

    public T first() {
        return get(0);
    }

    public T firstOrNull() {
        if (size() == 0) {
            return null;
        } else {
            return get(0);
        }
    }

    public Promise<T> firstPromise() {
        return new Promise<T>(executor -> {
            if (size() == 0) {
                executor.error(new RuntimeException("Array is empty"));
            } else {
                executor.result(get(0));
            }
        });
    }
}