package im.actor.messenger.storage.provider;

import com.droidkit.mvvm.CollectionBoxer;

import java.util.ArrayList;
import java.util.List;

import im.actor.model.mvvm.KeyValueEngine;
import im.actor.model.mvvm.KeyValueItem;

/**
 * Created by ex3ndr on 16.02.15.
 */
public class BoxerProvider<V, T extends KeyValueItem> implements KeyValueEngine<T> {

    private CollectionBoxer<Integer, T, V> boxer;

    public BoxerProvider(CollectionBoxer<Integer, T, V> boxer) {
        this.boxer = boxer;
    }

    @Override
    public void addOrUpdateItem(T item) {
        boxer.put((int) item.getEngineId(), item);
    }

    @Override
    public void addOrUpdateItems(List<T> values) {
        for (T t : values) {
            addOrUpdateItem(t);
        }
    }

    @Override
    public void removeItem(long id) {
        // Not supported
    }

    @Override
    public void removeItems(long[] ids) {
        // Not supported
    }

    @Override
    public void clear() {
        // Not supported
    }

    @Override
    public List<T> getAll() {
        // Not supported
        return new ArrayList<T>();
    }

    @Override
    public T getValue(long id) {
        return boxer.load((int) id);
    }
}
