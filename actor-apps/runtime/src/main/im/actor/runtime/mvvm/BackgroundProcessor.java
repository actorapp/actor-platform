package im.actor.runtime.mvvm;

public interface BackgroundProcessor<T> {
    void processInBackground(T item);
}
