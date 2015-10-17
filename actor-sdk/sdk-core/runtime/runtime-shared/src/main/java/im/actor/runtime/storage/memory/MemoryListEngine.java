package im.actor.runtime.storage.memory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserParser;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.DataInput;
import im.actor.runtime.storage.ListEngine;
import im.actor.runtime.storage.ListEngineItem;
import im.actor.runtime.storage.ListEngineRecord;
import im.actor.runtime.storage.ObjectCache;

public class MemoryListEngine<T extends BserObject & ListEngineItem> implements ListEngine<T> {

    private final Object LOCK = new Object();
    private final ObjectCache<Long, T> cache = new ObjectCache<Long, T>();
    private final MemoryListStorage storage;
    private final BserCreator<T> creator;

    public MemoryListEngine(MemoryListStorage storage, BserCreator<T> creator) {
        this.storage = storage;
        this.creator = creator;
    }

    @Override
    public void addOrUpdateItem(T item) {
        synchronized (LOCK) {
            cache.onObjectUpdated(item.getEngineId(), item);

            storage.updateOrAdd(
                    new ListEngineRecord(item.getEngineId(),
                            item.getEngineSort(),
                            item.getEngineSearch(),
                            item.toByteArray()));
        }
    }

    @Override
    public void addOrUpdateItems(List<T> items) {
        synchronized (LOCK) {
            ArrayList<ListEngineRecord> records = new ArrayList<ListEngineRecord>();
            for (T item : items) {
                cache.onObjectUpdated(item.getEngineId(), item);
                records.add(new ListEngineRecord(item.getEngineId(),
                        item.getEngineSort(),
                        item.getEngineSearch(),
                        item.toByteArray()));
            }
            storage.updateOrAdd(records);
        }
    }

    @Override
    public void replaceItems(List<T> items) {
        synchronized (LOCK) {
            cache.clear();

            ArrayList<ListEngineRecord> records = new ArrayList<ListEngineRecord>();
            for (T item : items) {
                cache.onObjectUpdated(item.getEngineId(), item);
                records.add(new ListEngineRecord(item.getEngineId(),
                        item.getEngineSort(),
                        item.getEngineSearch(),
                        item.toByteArray()));
            }
            storage.clear();
            storage.updateOrAdd(records);
        }
    }

    @Override
    public void removeItem(long key) {
        synchronized (LOCK) {
            cache.removeObject(key);
            storage.delete(key);
        }
    }

    @Override
    public void removeItems(long[] keys) {
        synchronized (LOCK) {
            for (long l : keys) {
                cache.removeObject(l);
            }
            storage.delete(keys);
        }
    }

    @Override
    public void clear() {
        synchronized (LOCK) {
            cache.clear();
            storage.clear();
        }
    }

    @Override
    public T getValue(long key) {
        synchronized (LOCK) {
            return loadValue(key);
        }
    }

    @Override
    public T getHeadValue() {
        synchronized (LOCK) {
            Long key = storage.getTopKey();
            if (key == null) {
                return null;
            }
            return loadValue(key);
        }
    }

    @Override
    public boolean isEmpty() {
        return storage.isEmpty();
    }

    @Override
    public int getCount() {
        return storage.getCount();
    }

    private T loadValue(long key) {
        T res = cache.lookup(key);
        if (res != null) {
            return res;
        }

        ListEngineRecord record = storage.loadItem(key);
        if (record == null) {
            return null;
        }
        try {
            res = creator.createInstance();
            res.parse(new BserValues(BserParser.deserialize(new DataInput(record.getData()))));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        cache.onObjectLoaded(key, res);
        return res;
    }
}
