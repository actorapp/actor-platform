/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.angular;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import java.util.ArrayList;
import java.util.List;

import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.engine.ListEngineItem;
import im.actor.model.js.JsMessenger;
import im.actor.model.js.entity.JsEntityConverter;
import im.actor.model.js.providers.storage.JsListEngine;
import im.actor.model.js.providers.storage.JsListEngineCallback;
import im.actor.model.log.Log;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class AngularList<T extends JavaScriptObject, V extends BserObject & ListEngineItem> implements JsListEngineCallback<V> {

    private final JsMessenger messenger;
    private final JsListEngine<V> listEngine;
    private final boolean isInverted;
    private final JsEntityConverter<V, T> entityConverter;

    private ArrayList<AngularListCallback<T>> callbacks = new ArrayList<AngularListCallback<T>>();

    private ArrayList<V> values;
    private JsArray<T> jsValues;

    public AngularList(JsListEngine<V> listEngine, boolean isInverted, JsEntityConverter<V, T> entityConverter, JsMessenger messenger) {
        this.messenger = messenger;
        this.listEngine = listEngine;
        this.entityConverter = entityConverter;
        this.isInverted = isInverted;

        this.values = new ArrayList<V>();
        this.jsValues = JavaScriptObject.createArray().cast();

        long[] rids = listEngine.getOrderedIds();
        for (long rid : rids) {
            V item = listEngine.getValue(rid);
            if (item == null) {
                Log.w("AngularList", "Unable to find item #" + rid);
                continue;
            }
            if (isInverted) {
                insert(jsValues, 0, entityConverter.convert(item, messenger));
                values.add(0, item);
            } else {
                values.add(item);
                jsValues.push(entityConverter.convert(item, messenger));
            }
        }
        listEngine.addListener(this);
    }

    public void subscribe(AngularListCallback<T> callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }
        callback.onCollectionChanged(jsValues);
    }

    public void unsubscribe(AngularListCallback<T> callback) {
        callbacks.remove(callback);
    }

    public void forceReconvert(long id) {
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).getEngineId() == id) {
                remove(jsValues, i);
                insert(jsValues, i, entityConverter.convert(values.get(i), messenger));
            }
        }
    }

    public void forceReconvert() {
        clear(jsValues);

        for (int i = 0; i < values.size(); i++) {
            jsValues.push(entityConverter.convert(values.get(i), messenger));
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

        if (isInverted) {
            for (int i = values.size() - 1; i >= 0; i--) {
                if (sortKey > values.get(i).getEngineSort()) {
                    values.add(i + 1, item);
                    insert(jsValues, i + 1, entityConverter.convert(item, messenger));
                    return;
                }
            }

            values.add(0, item);
            insert(jsValues, 0, entityConverter.convert(item, messenger));
        } else {
            for (int i = 0; i < values.size(); i++) {
                if (sortKey > values.get(i).getEngineSort()) {
                    values.add(i, item);
                    insert(jsValues, i, entityConverter.convert(item, messenger));
                    return;
                }
            }

            values.add(item);
            jsValues.push(entityConverter.convert(item, messenger));
        }
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
        for (AngularListCallback<T> callback : callbacks) {
            callback.onCollectionChanged(jsValues);
        }
    }

    protected native void clear(JsArray<T> array)/*-{ array.splice(0, array.length); }-*/;

    protected native void insert(JsArray<T> array, int index, T obj)/*-{ array.splice(index, 0, obj); }-*/;

    protected native void remove(JsArray<T> array, int index)/*-{ array.splice(index, 1); }-*/;
}