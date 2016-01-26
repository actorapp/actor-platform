package im.actor.runtime.function;

public interface Function<T, R> {
    R apply(T t);

    default R apply2(T t) {
        return apply(t);
    }
}
