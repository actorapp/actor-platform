package im.actor.core.runtime.generic.mvvm;

public interface BackgroundProcessor<T> {
    void processInBackground(T item);
}
