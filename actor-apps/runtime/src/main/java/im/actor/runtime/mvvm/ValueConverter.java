package im.actor.runtime.mvvm;

public interface ValueConverter<T, S> {
    S convert(T src);
}
