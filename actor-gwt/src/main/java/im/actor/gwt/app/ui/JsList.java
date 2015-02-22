package im.actor.gwt.app.ui;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import org.timepedia.exporter.client.NoExport;

import java.util.ArrayList;

import im.actor.gwt.app.storage.JsListEngine;
import im.actor.gwt.app.storage.JsListEngineCallback;
import im.actor.model.storage.ListEngineItem;

/**
 * Created by ex3ndr on 22.02.15.
 */
public class JsList<T extends JavaScriptObject, V extends ListEngineItem> implements JsListEngineCallback<V> {

    private ArrayList<V> values;
    private JsArray<T> jsValues;

    private ArrayList<JsListCallback<T>> simpleCallbacks = new ArrayList<JsListCallback<T>>();

    private JsEntityConverter<V, T> converter;

    public JsList(JsListEngine<V> engine, JsEntityConverter<V, T> converter) {
        this.values = new ArrayList<V>();
        this.jsValues = JavaScriptObject.createArray().cast();
        this.converter = converter;

        long[] ids = engine.getOrderedIds();
        for (long l : ids) {
            V v = engine.getValue(l);
            values.add(v);
            jsValues.push(converter.convert(v));
        }

        engine.addListener(this);
    }

    public void subscribe(JsListCallback<T> callback) {
        if (!simpleCallbacks.contains(callback)) {
            simpleCallbacks.add(callback);
        }
        callback.onCollectionChanged(jsValues);
    }

    public void unsubscribe(JsListCallback<T> callback) {
        simpleCallbacks.remove(callback);
    }

    private void notifySubscribers() {
        for (JsListCallback<T> callback : simpleCallbacks) {
            callback.onCollectionChanged(jsValues);
        }
    }

    @NoExport
    @Override
    public void onItemAddedOrUpdated(V item) {
        try {
            long id = item.getListId();
            long sortKey = item.getListSortKey();
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i).getListId() == id) {
                    values.remove(i);
                    remove(jsValues, i);
                    break;
                }
            }


            for (int i = 0; i < values.size(); i++) {
                if (values.get(i).getListSortKey() < sortKey) {
                    values.add(i, item);
                    insert(jsValues, i, converter.convert(item));
                    return;
                }
            }

            values.add(item);
            jsValues.push(converter.convert(item));
        } finally {
            notifySubscribers();
        }
    }

    @NoExport
    @Override
    public void onItemRemoved(long id) {
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).getListId() == id) {
                values.remove(i);
                remove(jsValues, i);
                break;
            }
        }

        notifySubscribers();
    }

    @NoExport
    @Override
    public void onClear() {
        values.clear();
        clear(jsValues);

        notifySubscribers();
    }

    protected native void clear(JsArray<T> array)/*-{ array.splice(0, array.length); }-*/;

    protected native void insert(JsArray<T> array, int index, T obj)/*-{ array.splice(index, 0, obj); }-*/;

    protected native void remove(JsArray<T> array, int index)/*-{ array.splice(index, 1); }-*/;
}