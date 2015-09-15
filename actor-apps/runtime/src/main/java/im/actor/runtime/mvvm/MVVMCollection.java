/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.mvvm;

import com.google.j2objc.annotations.ObjectiveCName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserParser;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.DataInput;
import im.actor.runtime.storage.KeyValueEngine;
import im.actor.runtime.storage.KeyValueItem;
import im.actor.runtime.storage.KeyValueRecord;
import im.actor.runtime.storage.KeyValueStorage;

public class MVVMCollection<T extends BserObject & KeyValueItem, V extends BaseValueModel<T>> {

    private final KeyValueStorage collectionStorage;
    private final HashMap<Long, V> values = new HashMap<Long, V>();
    private final ValueModelCreator<T, V> creator;
    private final BserCreator<T> bserCreator;

    private ProxyKeyValueEngine proxyKeyValueEngine;

    public MVVMCollection(KeyValueStorage collectionStorage, ValueModelCreator<T, V> creator,
                          BserCreator<T> bserCreator) {
        this.creator = creator;
        this.bserCreator = bserCreator;
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
                values.put(id, creator.create(res));
            } else {
                throw new RuntimeException("Unable to find object #" + id);
            }
        }
        return values.get(id);
    }

    @ObjectiveCName("clear")
    public synchronized void clear() {
        proxyKeyValueEngine.clear();
    }

    private void notifyChange(final List<T> items) {
        im.actor.runtime.Runtime.postToMainThread(new Runnable() {
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
        im.actor.runtime.Runtime.postToMainThread(new Runnable() {
            @Override
            public void run() {
                for (long l : ids) {
                    values.remove(l);
                }
            }
        });
    }

    private void notifyClear() {
        im.actor.runtime.Runtime.postToMainThread(new Runnable() {
            @Override
            public void run() {
                values.clear();
            }
        });
    }

    private class ProxyKeyValueEngine implements KeyValueEngine<T> {

        private final HashMap<Long, T> cache = new HashMap<Long, T>();

        @Override
        public synchronized void addOrUpdateItem(T item) {
            cache.put(item.getEngineId(), item);

            ArrayList<T> res = new ArrayList<T>();
            res.add(item);
            notifyChange(res);

            byte[] data = item.toByteArray();
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
                records.add(new KeyValueRecord(v.getEngineId(), v.toByteArray()));
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
                try {
                    T res = bserCreator.createInstance();
                    res.parse(new BserValues(BserParser.deserialize(new DataInput(data))));
                    cache.put(res.getEngineId(), res);
                    return res;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}