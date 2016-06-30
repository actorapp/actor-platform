package im.actor.runtime.function;

public interface ArrayFunction<T, R> {
    R apply(T[] t);
}