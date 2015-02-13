package im.actor.model.storage;

import im.actor.model.entity.User;
import im.actor.model.mvvm.KeyValueEngine;
import im.actor.model.mvvm.KeyValueItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ex3ndr on 13.02.15.
 */
public class MemoryKeyValueEngine<T extends KeyValueItem> implements KeyValueEngine<T> {
    private HashMap<Long, T> users = new HashMap<Long, T>();

    @Override
    public synchronized void addOrUpdateItem(T item) {
        users.put(item.getEngineId(), item);
    }

    @Override
    public synchronized void addOrUpdateItems(List<T> values) {
        for (T u : values) {
            users.put(u.getEngineId(), u);
        }
    }

    @Override
    public synchronized void removeItem(long id) {
        users.remove((long) id);
    }

    @Override
    public synchronized void removeItems(long[] ids) {
        for (long id : ids) {
            users.remove((long) id);
        }
    }

    @Override
    public synchronized void clear() {
        users.clear();
    }

    @Override
    public synchronized List<T> getAll() {
        return new ArrayList<T>(users.values());
    }

    @Override
    public synchronized T getValue(long id) {
        return users.get((long) id);
    }
}
