package com.droidkit.engine.list;

import com.droidkit.actors.*;
import com.droidkit.actors.typed.TypedCreator;
import com.droidkit.engine.Engines;
import com.droidkit.engine.cache.ObjectCache;
import com.droidkit.engine.list.actors.*;
import com.droidkit.engine.list.storage.StorageAdapter;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ListEngine<V> {

    static {
        Engines.init();
    }

    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    protected final Object LOCK = new Object();
    protected final ObjectCache<Long, V> cache;
    protected final ActorRef dbRawActor;
    protected final StorageActorInt<V> dbActor;
    protected final DataAdapter<V> dataAdapter;
    protected final CopyOnWriteArrayList<ListEngineCallback<V>> listeners;

    public ListEngine(StorageAdapter storageAdapter, DataAdapter<V> dataAdapter) {

        this.dbRawActor = ActorSystem.system().actorOf(StorageActor.storage(NEXT_ID.getAndIncrement(), storageAdapter, dataAdapter));
        this.dbActor = TypedCreator.typed(dbRawActor, StorageActorInt.class);

        this.cache = new ObjectCache<Long, V>();
        this.dataAdapter = dataAdapter;
        this.listeners = new CopyOnWriteArrayList<ListEngineCallback<V>>();
    }

    public DataAdapter<V> getDataAdapter() {
        return dataAdapter;
    }

    public void addListener(ListEngineCallback<V> callback) {
        listeners.add(callback);
    }

    public void removeListener(ListEngineCallback<V> callback) {
        listeners.remove(callback);
    }

    public void addOrUpdateItem(final V value) {
        synchronized (LOCK) {
            cache.onObjectUpdated(dataAdapter.getId(value), value);
            dbActor.updateOrAdd(value);

            for (ListEngineCallback<V> callback : listeners) {
                callback.addOrUpdate(value);
            }
        }
    }

    public void addOrUpdateItems(final List<V> values) {
        synchronized (LOCK) {
            for (V val : values) {
                cache.onObjectUpdated(dataAdapter.getId(val), val);
            }
            dbActor.updateOrAdd(values);

            for (ListEngineCallback<V> callback : listeners) {
                callback.addOrUpdate(values);
            }
        }
    }

    public void replaceItems(final List<V> values) {
        synchronized (LOCK) {
            cache.clear();
            for (V val : values) {
                cache.onObjectUpdated(dataAdapter.getId(val), val);
            }
            cache.startLock();
            dbActor.clear();
            dbActor.updateOrAdd(values);
            dbRawActor.send(new Runnable() {
                @Override
                public void run() {
                    cache.stopLock();
                }
            });
            for (ListEngineCallback<V> callback : listeners) {
                callback.onItemsReplaced(values);
            }
        }
    }

    // Removing items
    public void removeItem(final long key) {
        synchronized (LOCK) {
            cache.removeObject(key);
            dbActor.delete(key);
            for (ListEngineCallback<V> callback : listeners) {
                callback.onItemRemoved(key);
            }
        }
    }

    public void removeItems(final long[] keys) {
        synchronized (LOCK) {
            for (long key : keys) {
                cache.removeObject(key);
            }
            dbActor.delete(keys);

            for (ListEngineCallback<V> callback : listeners) {
                callback.onItemsRemoved(keys);
            }
        }
    }

    public void clear() {
        synchronized (LOCK) {
            cache.clear();
            cache.startLock();
            dbActor.clear();
            dbRawActor.send(new Runnable() {
                @Override
                public void run() {
                    cache.stopLock();
                }
            });

            for (ListEngineCallback<V> callback : listeners) {
                callback.onListClear();
            }
        }
    }

    public V getValue(long key) {
        V res = cache.lookup(key);
        if (res != null) {
            return res;
        } else {
            final Object lock = new Object();
            final List<V> resultList = new ArrayList<V>();
            synchronized (lock) {
                dbActor.loadItem(key, new LoadItemCallback<V>() {
                    @Override
                    public void onLoaded(V res) {
                        if (res != null) {
                            resultList.add(res);
                        }
                        synchronized (lock) {
                            lock.notify();
                        }
                    }
                });
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    return null;
                }

                if (resultList.size() > 0) {
                    res = resultList.get(0);
                    synchronized (LOCK) {
                        cache.onObjectLoaded(dataAdapter.getId(res), res);
                    }
                    return res;
                } else {
                    return null;
                }
            }
        }
    }

    public V getHeadValue() {
        final Object lock = new Object();
        final List<V> resultList = new ArrayList<V>();
        synchronized (lock) {
            dbActor.loadHeadValue(new LoadItemCallback<V>() {
                @Override
                public void onLoaded(V res) {
                    if (res != null) {
                        resultList.add(res);
                    }
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            });
            try {
                lock.wait();
            } catch (InterruptedException e) {
                return null;
            }

            if (resultList.size() > 0) {
                V res = resultList.get(0);
                synchronized (LOCK) {
                    cache.onObjectLoaded(dataAdapter.getId(res), res);
                }
                return res;
            } else {
                return null;
            }
        }
    }

    public int getCount() {
        final Object lock = new Object();
        final List<Integer> resultList = new ArrayList<Integer>();
        synchronized (lock) {
            dbActor.loadCount(new LoadItemCallback<Integer>() {
                @Override
                public void onLoaded(Integer res) {
                    if (res != null) {
                        resultList.add(res);
                    }
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            });
            try {
                lock.wait();
            } catch (InterruptedException e) {
                return 0;
            }

            if (resultList.size() > 0) {
                Integer res = resultList.get(0);
                return res;
            } else {
                return 0;
            }
        }
    }

    public void loadCenterInitial(long centerSortKey, int limit, final LoadCenterCallback<V> callback) {
        dbActor.loadCenterInitial(centerSortKey, limit, new LoadCenterCallback<V>() {
            @Override
            public void onLoaded(List<V> res, Object prevKey, Object nextKey) {
                synchronized (LOCK) {
                    for (V v : res) {
                        cache.onObjectLoaded(dataAdapter.getId(v), v);
                    }
                }
                callback.onLoaded(res, prevKey, nextKey);
            }
        });
    }

    public void loadTailInitial(int limit, final LoadCallback<V> callback) {
        dbActor.loadTailInitial(limit, new LoadCallback<V>() {
            @Override
            public void onLoaded(List<V> res, Object nextRef) {
                synchronized (LOCK) {
                    for (V v : res) {
                        cache.onObjectLoaded(dataAdapter.getId(v), v);
                    }
                }
                callback.onLoaded(res, nextRef);
            }
        });
    }

    public void loadTail(Object key, int limit, final LoadCallback<V> callback) {
        dbActor.loadTail(key, limit, new LoadCallback<V>() {
            @Override
            public void onLoaded(List<V> res, Object nextRef) {
                synchronized (LOCK) {
                    for (V v : res) {
                        cache.onObjectLoaded(dataAdapter.getId(v), v);
                    }
                }
                callback.onLoaded(res, nextRef);
            }
        });
    }

    public void loadHeadInitial(int limit, final LoadCallback<V> callback) {
        dbActor.loadHeadInitial(limit, new LoadCallback<V>() {
            @Override
            public void onLoaded(List<V> res, Object nextRef) {
                synchronized (LOCK) {
                    for (V v : res) {
                        cache.onObjectLoaded(dataAdapter.getId(v), v);
                    }
                }
                callback.onLoaded(res, nextRef);
            }
        });
    }

    public void loadHead(Object key, int limit, final LoadCallback<V> callback) {
        dbActor.loadHead(key, limit, new LoadCallback<V>() {
            @Override
            public void onLoaded(List<V> res, Object nextRef) {
                synchronized (LOCK) {
                    for (V v : res) {
                        cache.onObjectLoaded(dataAdapter.getId(v), v);
                    }
                }
                callback.onLoaded(res, nextRef);
            }
        });
    }

    public void loadTailInitial(String query, int limit, final LoadCallback<V> callback) {
        dbActor.loadTailInitial(query, limit, new LoadCallback<V>() {
            @Override
            public void onLoaded(List<V> res, Object nextRef) {
                synchronized (LOCK) {
                    for (V v : res) {
                        cache.onObjectLoaded(dataAdapter.getId(v), v);
                    }
                }
                callback.onLoaded(res, nextRef);
            }
        });
    }

    public void loadTail(String query, Object key, int limit, final LoadCallback<V> callback) {
        dbActor.loadTail(query, key, limit, new LoadCallback<V>() {
            @Override
            public void onLoaded(List<V> res, Object nextRef) {
                synchronized (LOCK) {
                    for (V v : res) {
                        cache.onObjectLoaded(dataAdapter.getId(v), v);
                    }
                }
                callback.onLoaded(res, nextRef);
            }
        });
    }

    public void loadHeadInitial(String query, int limit, final LoadCallback<V> callback) {
        dbActor.loadHeadInitial(query, limit, new LoadCallback<V>() {
            @Override
            public void onLoaded(List<V> res, Object nextRef) {
                synchronized (LOCK) {
                    for (V v : res) {
                        cache.onObjectLoaded(dataAdapter.getId(v), v);
                    }
                }
                callback.onLoaded(res, nextRef);
            }
        });
    }

    public void loadHead(String query, Object key, int limit, final LoadCallback<V> callback) {
        dbActor.loadHead(query, key, limit, new LoadCallback<V>() {
            @Override
            public void onLoaded(List<V> res, Object nextRef) {
                synchronized (LOCK) {
                    for (V v : res) {
                        cache.onObjectLoaded(dataAdapter.getId(v), v);
                    }
                }
                callback.onLoaded(res, nextRef);
            }
        });
    }
}