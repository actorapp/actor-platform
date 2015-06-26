package im.actor.model.mvvm;

public interface BackgroundProcessor<T> {
    void processInBackground(T item);
}
