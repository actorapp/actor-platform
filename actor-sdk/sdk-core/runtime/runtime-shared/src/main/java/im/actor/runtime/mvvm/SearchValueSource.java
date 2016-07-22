package im.actor.runtime.mvvm;

import java.util.List;

import im.actor.runtime.function.Consumer;

public interface SearchValueSource<T> {
    void loadResults(String query, Consumer<List<T>> callback);
}
