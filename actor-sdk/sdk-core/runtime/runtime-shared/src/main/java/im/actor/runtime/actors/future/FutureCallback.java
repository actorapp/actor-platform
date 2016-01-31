package im.actor.runtime.actors.future;

public interface FutureCallback<T> {

    void onResult(T res);

    void onError(Exception e);
}
