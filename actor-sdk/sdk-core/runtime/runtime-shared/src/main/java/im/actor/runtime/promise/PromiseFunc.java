package im.actor.runtime.promise;

public interface PromiseFunc<T> {
    void exec(PromiseResolver<T> executor);
}
