package im.actor.runtime.actors.ask;

public interface AskCallback<T> {
    void onResult(T obj);

    void onError(Exception e);
}
