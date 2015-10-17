/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.generic.storage;

import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.storage.ListEngineDisplayLoadCallback;
import im.actor.runtime.storage.ListEngineItem;
import im.actor.runtime.storage.ListStorageDisplayEx;

import static im.actor.runtime.actors.ActorSystem.system;

class AsyncStorageInt<T extends BserObject & ListEngineItem> {

    private static int NEXT_ID = 0;

    private ActorRef storageActor;

    public AsyncStorageInt(final ListStorageDisplayEx storage, final BserCreator<T> creator) {
        storageActor = system().actorOf(Props.create(AsyncStorageActor.class, new ActorCreator<AsyncStorageActor>() {
            @Override
            public AsyncStorageActor<T> create() {
                return new AsyncStorageActor<T>(storage, creator);
            }
        }).changeDispatcher("db"), "list_engine/" + NEXT_ID++);
    }

    public void addOrUpdateItems(List<T> items) {
        storageActor.send(new AsyncStorageActor.AddOrUpdate<T>(items));
    }

    public void replaceItems(List<T> items) {
        storageActor.send(new AsyncStorageActor.Replace<T>(items));
    }

    public void remove(long[] keys) {
        storageActor.send(new AsyncStorageActor.Remove(keys));
    }

    public void clear() {
        storageActor.send(new AsyncStorageActor.Clear());
    }

    // Sync

    public T getValue(long value) {
        final Object lock = new Object();
        final List<T> resultList = new ArrayList<T>();
        synchronized (lock) {
            storageActor.send(new AsyncStorageActor.LoadItem<T>(value, new AsyncStorageActor.LoadItemCallback<T>() {
                @Override
                public void onLoaded(T item) {
                    synchronized (lock) {
                        if (item != null) {
                            resultList.add(item);
                        }
                        lock.notify();
                    }
                }
            }));

            try {
                lock.wait();
            } catch (InterruptedException e) {
                return null;
            }

            if (resultList.size() > 0) {
                return resultList.get(0);
            } else {
                return null;
            }
        }
    }

    public T getHeadValue() {
        final Object lock = new Object();
        final List<T> resultList = new ArrayList<T>();
        synchronized (lock) {
            storageActor.send(new AsyncStorageActor.LoadHead<T>(new AsyncStorageActor.LoadItemCallback<T>() {
                @Override
                public void onLoaded(T item) {
                    synchronized (lock) {
                        if (item != null) {
                            resultList.add(item);
                        }
                        lock.notify();
                    }
                }
            }));

            try {
                lock.wait();
            } catch (InterruptedException e) {
                return null;
            }

            if (resultList.size() > 0) {
                return resultList.get(0);
            } else {
                return null;
            }
        }
    }

    public int getCount() {
        final Object lock = new Object();
        final List<Integer> resultList = new ArrayList<Integer>();
        synchronized (lock) {
            storageActor.send(new AsyncStorageActor.LoadCount(new AsyncStorageActor.LoadCountCallback() {
                @Override
                public void onLoaded(int count) {
                    synchronized (lock) {
                        resultList.add(count);
                        lock.notify();
                    }
                }
            }));

            try {
                lock.wait();
            } catch (InterruptedException e) {
                return 0;
            }

            if (resultList.size() > 0) {
                return resultList.get(0);
            } else {
                return 0;
            }
        }
    }

    // Display list

    public void loadForward(String query, Long afterSortKey, int limit, ListEngineDisplayLoadCallback<T> callback) {
        storageActor.send(new AsyncStorageActor.LoadForward<T>(query, afterSortKey, limit, callback));
    }

    public void loadBackward(String query, Long beforeSortKey, int limit, ListEngineDisplayLoadCallback<T> callback) {
        storageActor.send(new AsyncStorageActor.LoadBackward<T>(query, beforeSortKey, limit, callback));
    }

    public void loadCenter(long centerSortKey, int limit, ListEngineDisplayLoadCallback<T> callback) {
        storageActor.send(new AsyncStorageActor.LoadCenter<T>(centerSortKey, limit, callback));
    }
}
