package im.actor.runtime.generic.mvvm;

public interface BackgroundProcessor<T> {
    void processInBackground(T item);
}
