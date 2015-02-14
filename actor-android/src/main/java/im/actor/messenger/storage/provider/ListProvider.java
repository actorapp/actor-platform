package im.actor.messenger.storage.provider;

import java.util.List;

import im.actor.model.mvvm.ListEngine;
import im.actor.model.mvvm.ListEngineItem;

/**
 * Created by ex3ndr on 14.02.15.
 */
public class ListProvider<T extends ListEngineItem> implements ListEngine<T> {

    private com.droidkit.engine.list.ListEngine<T> srcEngine;

    public ListProvider(com.droidkit.engine.list.ListEngine<T> srcEngine) {
        this.srcEngine = srcEngine;
    }

    @Override
    public void addOrUpdateItem(T item) {
        srcEngine.addOrUpdateItem(item);
    }

    @Override
    public void addOrUpdateItems(List<T> values) {
        srcEngine.addOrUpdateItems(values);
    }

    @Override
    public void replaceItems(List<T> values) {
        srcEngine.replaceItems(values);
    }

    @Override
    public void removeItem(long id) {
        srcEngine.removeItem(id);
    }

    @Override
    public void removeItems(long[] ids) {
        srcEngine.removeItems(ids);
    }

    @Override
    public void clear() {
        srcEngine.clear();
    }

    @Override
    public T getValue(long id) {
        return srcEngine.getValue(id);
    }

    @Override
    public T getHeadValue() {
        return srcEngine.getHeadValue();
    }

    @Override
    public int getCount() {
        return srcEngine.getCount();
    }
}
