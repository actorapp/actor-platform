package im.actor.runtime.mvvm;

public interface ValueModelCreator<T, V extends BaseValueModel<T>> {
    V create(T baseValue);
}
