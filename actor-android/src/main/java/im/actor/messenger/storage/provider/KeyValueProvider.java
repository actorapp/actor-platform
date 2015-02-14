package im.actor.messenger.storage.provider;

import java.util.List;

import im.actor.model.mvvm.KeyValueEngine;
import im.actor.model.mvvm.KeyValueItem;

/**
 * Created by ex3ndr on 14.02.15.
 */
public class KeyValueProvider<T extends KeyValueItem> implements KeyValueEngine<T> {

    private com.droidkit.engine.keyvalue.KeyValueEngine<T> srcEngine;

    public KeyValueProvider(com.droidkit.engine.keyvalue.KeyValueEngine<T> srcEngine) {
        this.srcEngine = srcEngine;
    }

    @Override
    public void addOrUpdateItem(T item) {
        srcEngine.put(item);
    }

    @Override
    public void addOrUpdateItems(List<T> values) {
        srcEngine.putAll(values);
    }

    @Override
    public void removeItem(long id) {
        srcEngine.remove(id);
    }

    @Override
    public void removeItems(long[] ids) {
        for (long l : ids) {
            removeItem(l);
        }
    }

    @Override
    public void clear() {
        srcEngine.clear();
    }

    @Override
    public List<T> getAll() {
        return srcEngine.getAll();
    }

    @Override
    public T getValue(long id) {
        return srcEngine.get(id);
    }
}
