/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.actor.model.droidkit.engine.KeyValueEngine;
import im.actor.model.droidkit.engine.KeyValueItem;
import im.actor.model.droidkit.engine.KeyValueRecord;
import im.actor.model.droidkit.engine.KeyValueStorage;

public abstract class MVVMCollection<T extends KeyValueItem, V extends BaseValueModel<T>> {

    private HashMap<Long, V> values = new HashMap<Long, V>();
    private KeyValueStorage collectionStorage;
    private ProxyKeyValueEngine proxyKeyValueEngine;

    protected MVVMCollection(KeyValueStorage collectionStorage) {
        this.collectionStorage = collectionStorage;
        this.proxyKeyValueEngine = new ProxyKeyValueEngine();
    }

    @ObjectiveCName("getEngine")
    public KeyValueEngine<T> getEngine() {
        return proxyKeyValueEngine;
    }

    @ObjectiveCName("getWithId:")
    public synchronized V get(long id) {
        if (values.get(id) == null) {
            T res = proxyKeyValueEngine.getValue(id);
            if (res != null) {
                values.put(id, createNew(res));
            } else {
                throw new RuntimeException("Unable to find user #" + id);
            }
        }
        return values.get(id);
    }

    @ObjectiveCName("clear")
    public synchronized void clear() {
        proxyKeyValueEngine.clear();
    }

    private void notifyChange(final List<T> items) {
        MVVMEngine.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (T i : items) {
                    if (values.containsKey(i.getEngineId())) {
                        values.get(i.getEngineId()).update(i);
                    }
                }
            }
        });
    }

    private void notifyRemove(final long[] ids) {
        MVVMEngine.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (long l : ids) {
                    values.remove(l);
                }
            }
        });
    }

    private void notifyClear() {
        MVVMEngine.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                values.clear();
            }
        });
    }

    // Entity

    protected abstract V createNew(T raw);

    protected abstract byte[] serialize(T raw);

    protected abstract T deserialize(byte[] raw);

    private class ProxyKeyValueEngine implements KeyValueEngine<T> {

        private final HashMap<Long, T> cache = new HashMap<Long, T>();

        @Override
        public synchronized void addOrUpdateItem(T item) {
            cache.put(item.getEngineId(), item);

            ArrayList<T> res = new ArrayList<T>();
            res.add(item);
            notifyChange(res);

            byte[] data = serialize(item);
            collectionStorage.addOrUpdateItem(item.getEngineId(), data);
        }

        @Override
        public synchronized void addOrUpdateItems(List<T> values) {
            for (T t : values) {
                cache.put(t.getEngineId(), t);
            }

            notifyChange(values);

            ArrayList<KeyValueRecord> records = new ArrayList<KeyValueRecord>();
            for (T v : values) {
                records.add(new KeyValueRecord(v.getEngineId(), serialize(v)));
            }
            collectionStorage.addOrUpdateItems(records);
        }

        @Override
        public synchronized void removeItem(long id) {
            cache.remove(id);

            notifyRemove(new long[]{id});

            collectionStorage.removeItem(id);
        }

        @Override
        public synchronized void removeItems(long[] ids) {
            for (long l : ids) {
                cache.remove(l);
            }

            notifyRemove(ids);

            collectionStorage.removeItems(ids);
        }

        @Override
        public synchronized void clear() {
            cache.clear();
            notifyClear();
            collectionStorage.clear();
        }

        @Override
        public synchronized T getValue(long id) {
            if (cache.containsKey(id)) {
                return cache.get(id);
            }

            byte[] data = collectionStorage.getValue(id);
            if (data != null) {
                T res = deserialize(data);
                cache.put(res.getEngineId(), res);
                return res;
            } else {
                return null;
            }
        }
    }
}