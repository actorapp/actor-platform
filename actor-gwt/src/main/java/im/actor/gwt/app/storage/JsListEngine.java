package im.actor.gwt.app.storage;

import com.google.gwt.storage.client.Storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.actor.model.log.Log;
import im.actor.model.storage.ListEngine;
import im.actor.model.storage.ListEngineItem;

/**
 * Created by ex3ndr on 22.02.15.
 */
public abstract class JsListEngine<V extends ListEngineItem> implements ListEngine<V> {

    private static String TAG = "ListEngine";

    private JsListStorage listStorage;

    private HashMap<Long, V> cache = new HashMap<Long, V>();

    private ArrayList<JsListEngineCallback<V>> callbacks = new ArrayList<JsListEngineCallback<V>>();

    public JsListEngine(String prefix, Storage storage) {
        this.listStorage = new JsListStorage(prefix, storage);
    }

    @Override
    public void addOrUpdateItem(V item) {
        Log.d(TAG, "addOrUpdateItem");
        cache.put(item.getListId(), item);
        for (JsListEngineCallback<V> callback : callbacks) {
            callback.onItemAddedOrUpdated(item);
        }
        listStorage.addOrUpdateItem(item.getListId(), item.getListSortKey(),
                serialize(item));
    }

    @Override
    public void addOrUpdateItems(List<V> values) {
        for (V v : values) {
            addOrUpdateItem(v);
        }
    }

    @Override
    public void replaceItems(List<V> values) {
        cache.clear();
        for (JsListEngineCallback<V> callback : callbacks) {
            callback.onClear();
        }
        listStorage.clear();
        addOrUpdateItems(values);
    }

    @Override
    public void removeItem(long id) {
        Log.d(TAG, "removeItem");
        cache.remove(id);
        for (JsListEngineCallback<V> callback : callbacks) {
            callback.onItemRemoved(id);
        }
        listStorage.remove(id);
    }

    @Override
    public void removeItems(long[] ids) {
        for (long id : ids) {
            removeItem(id);
        }
    }

    @Override
    public void clear() {
        Log.d(TAG, "clear");
        cache.clear();
        for (JsListEngineCallback<V> callback : callbacks) {
            callback.onClear();
        }
        listStorage.clear();
    }

    @Override
    public V getValue(long id) {
        Log.d(TAG, "getValue");
        if (cache.containsKey(id)) {
            return cache.get(id);
        }
        byte[] res = listStorage.getItem(id);
        if (res != null) {
            return deserialize(res);
        }
        return null;
    }

    @Override
    public V getHeadValue() {
        Log.d(TAG, "getHeadValue");
        long[] orderedIds = listStorage.getOrderedIds();
        if (orderedIds.length > 0) {
            return getValue(orderedIds[0]);
        }
        return null;
    }

    @Override
    public int getCount() {
        return listStorage.getCount();
    }

    public long[] getOrderedIds() {
        return listStorage.getOrderedIds();
    }

    public void addListener(JsListEngineCallback<V> callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }

    public void removeListener(JsListEngineCallback<V> callback) {
        callbacks.remove(callback);
    }

    protected abstract byte[] serialize(V item);

    protected abstract V deserialize(byte[] data);
}