package im.actor.model.js.angular;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.engine.ListEngineItem;
import im.actor.model.i18n.I18nEngine;
import im.actor.model.js.JsMessenger;
import im.actor.model.js.entity.JsEntityConverter;
import im.actor.model.js.providers.storage.JsListEngine;
import im.actor.model.js.providers.storage.JsListEngineCallback;

import java.util.ArrayList;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class AngularList<T extends JavaScriptObject, V extends BserObject & ListEngineItem> implements JsListEngineCallback<V> {

    private final I18nEngine formatter;
    private final JsListEngine<V> listEngine;
    private final JsEntityConverter<V, T> entityConverter;

    private ArrayList<AngularListCallback<T>> callbacks = new ArrayList<AngularListCallback<T>>();

    private ArrayList<V> values;
    private JsArray<T> jsValues;

    public AngularList(JsListEngine<V> listEngine, JsEntityConverter<V, T> entityConverter, I18nEngine formatter) {
        this.formatter = formatter;
        this.listEngine = listEngine;
        this.entityConverter = entityConverter;

        this.values = new ArrayList<V>();
        this.jsValues = JavaScriptObject.createArray().cast();

        long[] rids = listEngine.getOrderedIds();
        for (long rid : rids) {
            V item = listEngine.getValue(rid);
            values.add(item);
            jsValues.push(entityConverter.convert(item, formatter));
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

    @Override
    public void onItemAddedOrUpdated(V item) {
        try {
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
                if (values.get(i).getEngineSort() < sortKey) {
                    values.add(i, item);
                    insert(jsValues, i, entityConverter.convert(item, formatter));
                    return;
                }
            }

            values.add(item);
            jsValues.push(entityConverter.convert(item, formatter));
        } finally {
            notifySubscribers();
        }
    }

    @Override
    public void onItemRemoved(long id) {
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).getEngineId() == id) {
                values.remove(i);
                remove(jsValues, i);
                break;
            }
        }

        notifySubscribers();
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