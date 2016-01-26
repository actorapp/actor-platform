package im.actor.runtime.function;

public interface Function<T, R> {
    R apply(T t);
}
