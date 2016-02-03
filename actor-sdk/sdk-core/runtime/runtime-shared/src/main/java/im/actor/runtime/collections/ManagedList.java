package im.actor.runtime.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;
import im.actor.runtime.function.Predicate;
import im.actor.runtime.function.Supplier;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.PromiseResolver;

public class ManagedList<T> extends ArrayList<T> {

    public static <T> ManagedList<T> of(Collection<? extends T> collection) {
        return new ManagedList<>(collection);
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

    public ManagedList<T> unique(Comparator<T> comparator) {
        ManagedList<T> res = new ManagedList<>();
        outer:
        for (T t : this) {
            for (T t2 : res) {
                if (comparator.compare(t, t2) == 0) {
                    continue outer;
                }
            }
            res.add(t);
        }
        return res;
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

    public ManagedList<T> forEach(Consumer<T> consumer) {
        for (T t : this) {
            consumer.apply(t);
        }
        return this;
    }

    public T first() {
        return get(0);
    }

    public Promise<T> firstPromise() {
        return new Promise<T>(new PromiseFunc<T>() {
            @Override
            public void exec(PromiseResolver<T> executor) {
                if (size() == 0) {
                    executor.error(new RuntimeException("Array is empty"));
                } else {
                    executor.result(get(0));
                }
            }
        });
    }
}