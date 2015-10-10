/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.mvvm;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.Log;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.js.storage.JsListEngine;
import im.actor.runtime.js.storage.JsListEngineCallback;
import im.actor.runtime.mvvm.PlatformDisplayList;
import im.actor.runtime.storage.ListEngineItem;

public class JsDisplayList<T extends JavaScriptObject, V extends BserObject & ListEngineItem> implements JsListEngineCallback<V>,
        PlatformDisplayList<V> {

    private final JsListEngine<V> listEngine;
    private final JsEntityConverter<V, T> entityConverter;

    private ArrayList<JsDisplayListCallback<T>> callbacks = new ArrayList<JsDisplayListCallback<T>>();
    private ArrayList<JsDisplayListCallback<T>> callbacksInverted = new ArrayList<JsDisplayListCallback<T>>();

    private ArrayList<V> values;
    private JsArray<T> jsValues;

    public JsDisplayList(JsListEngine<V> listEngine, JsEntityConverter<V, T> entityConverter) {
        this.listEngine = listEngine;
        this.entityConverter = entityConverter;

        this.values = new ArrayList<V>();
        this.jsValues = JavaScriptObject.createArray().cast();

        long[] rids = listEngine.getOrderedIds();
        for (long rid : rids) {
            V item = listEngine.getValue(rid);
            if (item == null) {
                Log.w("AngularList", "Unable to find item #" + rid);
                continue;
            }
            values.add(item);
            jsValues.push(entityConverter.convert(item));
        }
        listEngine.addListener(this);
    }

    public void subscribe(JsDisplayListCallback<T> callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }

        // new JsArray<T>(jsValues)

        callback.onCollectionChanged(jsValues);
    }

    public void unsubscribe(JsDisplayListCallback<T> callback) {
        callbacks.remove(callback);
    }

    public void subscribeInverted(JsDisplayListCallback<T> callback) {
        if (!callbacksInverted.contains(callback)) {
            callbacksInverted.add(callback);
        }

        callback.onCollectionChanged(reverse(jsValues));
    }

    public void unsubscribeInverted(JsDisplayListCallback<T> callback) {
        callbacksInverted.remove(callback);
    }

    public void forceReconvert(long id) {
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).getEngineId() == id) {
                remove(jsValues, i);
                insert(jsValues, i, entityConverter.convert(values.get(i)));
            }
        }
    }

    public void forceReconvert() {
        clear(jsValues);

        for (int i = 0; i < values.size(); i++) {
            jsValues.push(entityConverter.convert(values.get(i)));
        }

        notifySubscribers();
    }

    public ArrayList<V> getRawItems() {
        return values;
    }

    private void addItemOrUpdateImpl(V item) {
        long id = item.getEngineId();
        long sortKey = item.getEngineSort();
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).getEngineId() == id) {
                values.remove(i);
                remove(jsValues, i);
                break;
            }
        }

        for (int i = 0; i < values.size(); i++) {
            if (sortKey > values.get(i).getEngineSort()) {
                values.add(i, item);
                insert(jsValues, i, entityConverter.convert(item));
                return;
            }
        }

        values.add(item);
        jsValues.push(entityConverter.convert(item));
    }

    private void remoteItemImpl(long id) {
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).getEngineId() == id) {
                values.remove(i);
                remove(jsValues, i);
                break;
            }
        }
    }

    @Override
    public void onItemAddedOrUpdated(V item) {
        addItemOrUpdateImpl(item);
        notifySubscribers();
    }

    @Override
    public void onItemsAddedOrUpdated(List<V> items) {
        for (V item : items) {
            addItemOrUpdateImpl(item);
        }
        notifySubscribers();
    }

    @Override
    public void onItemRemoved(long id) {
        remoteItemImpl(id);
        notifySubscribers();
    }

    @Override
    public void onItemsRemoved(long[] ids) {
        for (long id : ids) {
            remoteItemImpl(id);
        }
        notifySubscribers();
    }

    @Override
    public void onItemsReplaced(List<V> items) {
        values.clear();
        clear(jsValues);
        onItemsAddedOrUpdated(items);
    }

    @Override
    public void onClear() {
        values.clear();
        clear(jsValues);
        notifySubscribers();
    }

    private void notifySubscribers() {
        for (JsDisplayListCallback<T> callback : callbacks) {
            callback.onCollectionChanged(jsValues);
        }

        if (callbacksInverted.size() > 0) {
            JsArray<T> rev = reverse(jsValues);
            for (JsDisplayListCallback<T> callback : callbacksInverted) {
                callback.onCollectionChanged(rev);
            }
        }
    }

    protected native void clear(JsArray<T> array)/*-{ array.splice(0, array.length); }-*/;

    protected native void insert(JsArray<T> array, int index, T obj)/*-{ array.splice(index, 0, obj); }-*/;

    protected native void remove(JsArray<T> array, int index)/*-{ array.splice(index, 1); }-*/;

    protected native JsArray<T> reverse(JsArray<T> array)/*-{ return array.slice().reverse(); }-*/;

    @Override
    public void initCenter(long rid) {
        // Nothing to do
    }

    @Override
    public void initTop() {
        // Nothing to do
    }

    @Override
    public void initEmpty() {
        // Nothing to do
    }
}