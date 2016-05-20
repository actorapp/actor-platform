package im.actor.runtime.mvvm;

public interface ValueDefaultCreator<T> {
    T createDefaultInstance(long id);
}
