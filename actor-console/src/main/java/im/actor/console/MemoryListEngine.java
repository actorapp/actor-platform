package im.actor.console;

import im.actor.model.mvvm.ListEngine;
import im.actor.model.mvvm.ListEngineItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class MemoryListEngine<V extends ListEngineItem> implements ListEngine<V> {

    private HashMap<Long, V> items = new HashMap<Long, V>();
    private ArrayList<V> sortList = new ArrayList<V>();
    private ArrayList<EngineListener> listeners = new ArrayList<EngineListener>();

    public MemoryListEngine() {
    }

    public void addListener(EngineListener l) {
        listeners.add(l);
    }

    public void removeListener(EngineListener l) {
        listeners.remove(l);
    }

    private void updateSort() {
        sortList.clear();
        sortList.addAll(items.values());
        sortList.sort(new Comparator<V>() {
            @Override
            public int compare(V o1, V o2) {
                return compare(o2.getListSortKey(), o1.getListSortKey());
            }

            public int compare(long x, long y) {
                return (x < y) ? -1 : ((x == y) ? 0 : 1);
            }
        });

        for (EngineListener l : listeners) {
            l.onItemsChanged();
        }
    }

    public ArrayList<V> getList() {
        return (ArrayList<V>) sortList.clone();
    }

    @Override
    public void addOrUpdateItem(V item) {
        items.put(item.getListId(), item);
        updateSort();
    }

    @Override
    public void addOrUpdateItems(List<V> values) {
        for (V v : values) {
            addOrUpdateItem(v);
        }
    }

    @Override
    public void replaceItems(List<V> values) {
        items.clear();
        addOrUpdateItems(values);
    }

    @Override
    public void removeItem(long id) {
        items.remove(id);
        updateSort();
    }

    @Override
    public void removeItems(long[] ids) {
        for (long l : ids) {
            items.remove(l);
        }
        updateSort();
    }

    @Override
    public void clear() {
        items.clear();
        updateSort();
    }

    @Override
    public V getValue(long id) {
        return items.get(id);
    }

    @Override
    public V getHeadValue() {
        return null;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public interface EngineListener {
        public void onItemsChanged();
    }
}
