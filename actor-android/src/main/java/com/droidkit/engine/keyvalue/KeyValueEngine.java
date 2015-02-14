package com.droidkit.engine.keyvalue;

import android.support.v4.util.LruCache;
import com.droidkit.actors.ActorRef;
import com.droidkit.actors.ActorSystem;
import com.droidkit.actors.Props;
import com.droidkit.engine.Engines;
import com.droidkit.engine._internal.RunnableActor;
import com.droidkit.engine.common.ValuesCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class KeyValueEngine<V> {

    static {
        Engines.init();
    }

    private static final int DEFAULT_MEMORY_CACHE = 128;

    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    protected final ActorRef dbActor;

    private LruCache<Long, V> inMemoryLruCache;

    private final StorageAdapter<V> storageAdapter;

    private final com.droidkit.engine.keyvalue.DataAdapter<V> dataAdapter;

    public KeyValueEngine(StorageAdapter<V> storageAdapter,
                          com.droidkit.engine.keyvalue.DataAdapter<V> dataAdapter) {
        this(storageAdapter, dataAdapter, DEFAULT_MEMORY_CACHE);

    }

    public KeyValueEngine(StorageAdapter<V> storageAdapter,
                          DataAdapter<V> dataAdapter,
                          int inMemoryCacheSize) {
        this.inMemoryLruCache = new LruCache<Long, V>(inMemoryCacheSize);
        this.storageAdapter = storageAdapter;
        this.dataAdapter = dataAdapter;
        this.dbActor = ActorSystem.system().actorOf(
                Props.create(RunnableActor.class)
                        .changeDispatcher("db"), "key_value_db_" + NEXT_ID.getAndIncrement());
    }

    public void put(final V value) {
        inMemoryLruCache.put(dataAdapter.getId(value), value);
        dbActor.send(new Runnable() {
            @Override
            public void run() {
                storageAdapter.insertOrReplaceSingle(value);
            }
        });
    }

    public void putSync(final V value) {
        inMemoryLruCache.put(dataAdapter.getId(value), value);
        storageAdapter.insertOrReplaceSingle(value);
    }

    public void putAll(final List<V> values) {
        for (V v : values) {
            inMemoryLruCache.put(dataAdapter.getId(v), v);
        }
        dbActor.send(new Runnable() {
            @Override
            public void run() {
                storageAdapter.insertOrReplaceBatch(values);
            }
        });
    }

    public void putAllSync(final List<V> values) {
        for (V v : values) {
            inMemoryLruCache.put(dataAdapter.getId(v), v);
        }
        storageAdapter.insertOrReplaceBatch(values);
    }

    public V get(final long id) {
        V value = inMemoryLruCache.get(id);
        if (value == null) {
            value = storageAdapter.getById(id);
            if (value != null) {
                inMemoryLruCache.put(id, value);
            }
            return value;
        }
        return value;
    }

    public void getAll(final ValuesCallback<V> callback) {
        dbActor.send(new Runnable() {
            @Override
            public void run() {
                callback.values(getAll());
            }
        });
    }

    public ArrayList<V> getAll() {
        return storageAdapter.loadAll();
    }

    public void clear() {
        inMemoryLruCache.evictAll();
        dbActor.send(new Runnable() {
            @Override
            public void run() {
                storageAdapter.deleteAll();
            }
        });
    }

    public void clearSync() {
        inMemoryLruCache.evictAll();
        storageAdapter.deleteAll();
    }

    public void remove(final long id) {
        inMemoryLruCache.remove(id);
        dbActor.send(new Runnable() {
            @Override
            public void run() {
                storageAdapter.deleteSingle(id);
            }
        });
    }

    public void removeSync(final long id) {
        inMemoryLruCache.remove(id);
        storageAdapter.deleteSingle(id);
    }
}
