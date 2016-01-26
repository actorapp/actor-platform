package im.actor.runtime.streams;

import java.util.Collection;

public class Streams {

    public static <T> Stream<T> toStream(T[] array) {
        return consumer -> {
            for (T t : array) {
                consumer.apply(t);
            }
        };
    }

    public static <T> Stream<T> toStream(Collection<T> collection) {
        return consumer -> {
            for (T t : collection) {
                consumer.apply(t);
            }
        };
    }
}
