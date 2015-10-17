/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.generic.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.actors.Actor;
import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.storage.ListEngineDisplayLoadCallback;
import im.actor.runtime.storage.ListEngineItem;
import im.actor.runtime.storage.ListEngineRecord;
import im.actor.runtime.storage.ListStorageDisplayEx;

class AsyncStorageActor<T extends BserObject & ListEngineItem> extends Actor {
    private final ListStorageDisplayEx storage;
    private final BserCreator<T> creator;

    public AsyncStorageActor(ListStorageDisplayEx storage, BserCreator<T> creator) {
        this.storage = storage;
        this.creator = creator;
    }

    public void addOrUpdate(List<T> items) {
        if (items.size() == 1) {
            T item = items.get(0);
            storage.updateOrAdd(new ListEngineRecord(item.getEngineId(), item.getEngineSort(),
                    item.getEngineSearch(), item.toByteArray()));
        } else if (items.size() > 0) {
            List<ListEngineRecord> updated = new ArrayList<ListEngineRecord>();
            for (T i : items) {
                updated.add(new ListEngineRecord(i.getEngineId(), i.getEngineSort(),
                        i.getEngineSearch(), i.toByteArray()));
            }
            storage.updateOrAdd(updated);
        }
    }

    public void replace(List<T> items) {
        List<ListEngineRecord> updated = new ArrayList<ListEngineRecord>();
        for (T i : items) {
            updated.add(new ListEngineRecord(i.getEngineId(), i.getEngineSort(),
                    i.getEngineSearch(), i.toByteArray()));
        }
        storage.clear();
        storage.updateOrAdd(updated);
    }

    public void remove(long[] keys) {
        if (keys.length == 1) {
            storage.delete(keys[0]);
        } else if (keys.length > 0) {
            storage.delete(keys);
        }
    }

    public void clear() {
        storage.clear();
    }

    public void loadItem(long key, LoadItemCallback<T> callback) {
        ListEngineRecord record = storage.loadItem(key);
        if (record != null) {
            try {
                T res = Bser.parse(creator.createInstance(), record.getData());
                callback.onLoaded(res);
            } catch (IOException e) {
                e.printStackTrace();
                callback.onLoaded(null);
            }
        } else {
            callback.onLoaded(null);
        }
    }

    public void loadHead(LoadItemCallback<T> callback) {
        List<ListEngineRecord> records = storage.loadForward(null, 1);

        if (records.size() != 1) {
            callback.onLoaded(null);
            return;
        }

        ListEngineRecord record = records.get(0);
        try {
            callback.onLoaded(Bser.parse(creator.createInstance(), record.getData()));
        } catch (IOException e) {
            e.printStackTrace();
            callback.onLoaded(null);
        }
    }

    public void loadCount(LoadCountCallback callback) {
        callback.onLoaded(storage.getCount());
    }

    public void loadForward(String query, Long topSortKey, int limit, ListEngineDisplayLoadCallback<T> callback) {
        ArrayList<T> res;
        if (query == null) {
            res = convertList(storage.loadForward(topSortKey, limit));
        } else {
            res = convertList(storage.loadForward(query, topSortKey, limit));
        }

        callCallback(callback, res);
    }

    public void loadBackward(String query, Long topSortKey, int limit, ListEngineDisplayLoadCallback<T> callback) {
        ArrayList<T> res;
        if (query == null) {
            res = convertList(storage.loadBackward(topSortKey, limit));
        } else {
            res = convertList(storage.loadBackward(query, topSortKey, limit));
        }

        callCallback(callback, res);
    }

    public void loadCenter(Long centerSortKey, int limit, ListEngineDisplayLoadCallback<T> callback) {
        ArrayList<T> res;
        res = convertList(storage.loadCenter(centerSortKey, limit));
        callCallback(callback, res);
    }

    private void callCallback(ListEngineDisplayLoadCallback<T> callback, List<T> res) {
        if (res.size() == 0) {
            callback.onLoaded(res, 0, 0);
        } else {
            long topSort, bottomSort;
            topSort = bottomSort = res.get(0).getEngineSort();

            for (T t : res) {
                long sort = t.getEngineSort();
                if (topSort < sort) {
                    topSort = sort;
                }
                if (bottomSort > sort) {
                    bottomSort = sort;
                }
            }

            callback.onLoaded(res, topSort, bottomSort);
        }
    }

    private ArrayList<T> convertList(List<ListEngineRecord> records) {
        ArrayList<T> res = new ArrayList<T>();
        for (ListEngineRecord record : records) {
            try {
                T loaded = Bser.parse(creator.createInstance(), record.getData());
                res.add(loaded);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof AddOrUpdate) {
            addOrUpdate(((AddOrUpdate) message).getItems());
        } else if (message instanceof Remove) {
            remove(((Remove) message).getKeys());
        } else if (message instanceof Clear) {
            clear();
        } else if (message instanceof Replace) {
            replace(((Replace) message).getItems());
        } else if (message instanceof LoadItem) {
            loadItem(((LoadItem) message).getKey(), ((LoadItem) message).getCallback());
        } else if (message instanceof LoadCount) {
            loadCount(((LoadCount) message).getCallback());
        } else if (message instanceof LoadHead) {
            loadHead(((LoadHead) message).getCallback());
        } else if (message instanceof LoadForward) {
            loadForward(((LoadForward) message).getQuery(), ((LoadForward) message).getTopSortKey(),
                    ((LoadForward) message).getLimit(), ((LoadForward) message).getCallback());
        } else if (message instanceof LoadBackward) {
            loadBackward(((LoadBackward) message).getQuery(), ((LoadBackward) message).getTopSortKey(),
                    ((LoadBackward) message).getLimit(), ((LoadBackward) message).getCallback());
        }else if (message instanceof LoadCenter) {
            loadCenter(((LoadCenter) message).getCenterSortKey(),
                    ((LoadCenter) message).getLimit(), ((LoadCenter) message).getCallback());
        } else {
            drop(message);
        }
    }

    public static class AddOrUpdate<T extends BserObject & ListEngineItem> {
        private List<T> items;

        public AddOrUpdate(List<T> items) {
            this.items = items;
        }

        public List<T> getItems() {
            return items;
        }
    }

    public static class Replace<T extends BserObject & ListEngineItem> {
        private List<T> items;

        public Replace(List<T> items) {
            this.items = items;
        }

        public List<T> getItems() {
            return items;
        }
    }

    public static class Remove {
        private long[] keys;

        public Remove(long[] keys) {
            this.keys = keys;
        }

        public long[] getKeys() {
            return keys;
        }
    }

    public static class Clear {

    }

    public static class LoadItem<T extends BserObject & ListEngineItem> {
        private long key;
        private LoadItemCallback<T> callback;

        public LoadItem(long key, LoadItemCallback<T> callback) {
            this.key = key;
            this.callback = callback;
        }

        public long getKey() {
            return key;
        }

        public LoadItemCallback<T> getCallback() {
            return callback;
        }
    }

    public static class LoadCount {
        private LoadCountCallback callback;

        public LoadCount(LoadCountCallback callback) {
            this.callback = callback;
        }

        public LoadCountCallback getCallback() {
            return callback;
        }
    }

    public static class LoadHead<T extends BserObject & ListEngineItem> {
        private LoadItemCallback<T> callback;

        public LoadHead(LoadItemCallback<T> callback) {
            this.callback = callback;
        }

        public LoadItemCallback<T> getCallback() {
            return callback;
        }
    }

    public static class LoadForward<T extends BserObject & ListEngineItem> {
        private String query;
        private Long topSortKey;
        private int limit;
        private ListEngineDisplayLoadCallback<T> callback;

        public LoadForward(String query, Long topSortKey, int limit, ListEngineDisplayLoadCallback<T> callback) {
            this.query = query;
            this.topSortKey = topSortKey;
            this.limit = limit;
            this.callback = callback;
        }

        public String getQuery() {
            return query;
        }

        public Long getTopSortKey() {
            return topSortKey;
        }

        public int getLimit() {
            return limit;
        }

        public ListEngineDisplayLoadCallback<T> getCallback() {
            return callback;
        }
    }

    public static class LoadBackward<T extends BserObject & ListEngineItem> {
        private String query;
        private Long topSortKey;
        private int limit;
        private ListEngineDisplayLoadCallback<T> callback;

        public LoadBackward(String query, Long topSortKey, int limit, ListEngineDisplayLoadCallback<T> callback) {
            this.query = query;
            this.topSortKey = topSortKey;
            this.limit = limit;
            this.callback = callback;
        }

        public String getQuery() {
            return query;
        }

        public Long getTopSortKey() {
            return topSortKey;
        }

        public int getLimit() {
            return limit;
        }

        public ListEngineDisplayLoadCallback<T> getCallback() {
            return callback;
        }
    }

    public static class LoadCenter<T extends BserObject & ListEngineItem> {
        private Long centerSortKey;
        private int limit;
        private ListEngineDisplayLoadCallback<T> callback;

        public LoadCenter(Long centerSortKey, int limit, ListEngineDisplayLoadCallback<T> callback) {
            this.centerSortKey = centerSortKey;
            this.limit = limit;
            this.callback = callback;
        }
        public Long getCenterSortKey() {
            return centerSortKey;
        }

        public int getLimit() {
            return limit;
        }

        public ListEngineDisplayLoadCallback<T> getCallback() {
            return callback;
        }
    }

    public interface LoadItemCallback<T extends BserObject & ListEngineItem> {
        public void onLoaded(T item);
    }

    public interface LoadCountCallback {
        public void onLoaded(int count);
    }
}
