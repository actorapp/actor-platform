package im.actor.runtime.streams;

import java.util.ArrayList;

import im.actor.runtime.function.BiConsumer;
import im.actor.runtime.function.BiFunction;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;

public interface Stream<T> {

    void forEach(Consumer<T> consumer);

    default void forEachRemaining(Consumer<T> consumer) {
        forEach(consumer);
    }

    default <R> Stream<R> map(Function<T, R> fun) {
        return consumer1 -> Stream.this.forEach(t -> consumer1.apply(fun.apply(t)));
    }

    default <R> Stream<R> mapAsync(BiConsumer<T, Consumer<R>> fun) {
        return consumer -> Stream.this.forEach(t -> fun.accept(t, consumer));
    }

    default Stream<T> concat(Stream<T> next) {
        return consumer -> {
            Stream.this.forEachRemaining(consumer);
            next.forEachRemaining(consumer);
        };
    }

    default <R> R reduce(R init, BiFunction<T, R, R> fun) {
        final Object[] res = {init};
        forEachRemaining(new Consumer<T>() {
            @Override
            public void apply(T t) {
                res[0] = fun.apply(t, (R) res[0]);
            }
        });
        return (R) res[0];
    }

    default ArrayList<T> toList() {
        ArrayList<T> res = new ArrayList<T>();
        forEachRemaining(t -> res.add(t));
        return res;
    }

    default T[] toArray() {
        return (T[]) toList().toArray();
    }
}