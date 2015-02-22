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
public abstract class JsBaseList<T extends JavaScriptObject, V extends ListEngineItem> implements JsListEngineCallback<V> {

    private JsListEngine<V> engine;
    private ArrayList<V> values;
    private JsArray<T> jsValues;

    public JsBaseList(JsListEngine<V> engine) {
        this.engine = engine;
        this.values = new ArrayList<V>();
        this.jsValues = JavaScriptObject.createArray().cast();

        long[] ids = engine.getOrderedIds();
        for (long l : ids) {
            V v = engine.getValue(l);
            values.add(v);
            jsValues.push(convert(v));
        }

        engine.addListener(this);
    }

    protected JsArray<T> getJsValues() {
        return jsValues;
    }

    protected abstract T convert(V src);

    // Implementation JsListEngineCallback

    @NoExport
    @Override
    public void onItemAddedOrUpdated(V item) {
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
                insert(jsValues, i, convert(item));
                return;
            }
        }

        values.add(item);
        jsValues.push(convert(item));
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
    }

    @NoExport
    @Override
    public void onClear() {
        values.clear();
        clear(jsValues);
    }

    protected native void clear(JsArray<T> array)/*-{ array.splice(0, array.length); }-*/;

    protected native void insert(JsArray<T> array, int index, T obj)/*-{ array.splice(index, 0, obj); }-*/;

    protected native void remove(JsArray<T> array, int index)/*-{ array.splice(index, 1); }-*/;
}