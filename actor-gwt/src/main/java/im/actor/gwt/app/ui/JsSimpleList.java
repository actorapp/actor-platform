package im.actor.gwt.app.ui;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import org.timepedia.exporter.client.NoExport;

import im.actor.gwt.app.storage.JsListEngine;
import im.actor.gwt.app.storage.JsListEngineCallback;
import im.actor.model.storage.ListEngineItem;

/**
 * Created by ex3ndr on 22.02.15.
 */
public abstract class JsSimpleList<T extends JavaScriptObject, V extends ListEngineItem> implements JsListEngineCallback<V> {

    private JsListEngine<V> engine;
    private JsArray<T> jsValues;

    public JsSimpleList(JsListEngine<V> engine) {
        this.engine = engine;
        updateValues();
        engine.addListener(this);
    }

    private void updateValues() {
        JsArray<T> res = JavaScriptObject.createArray().cast();
        long[] ids = engine.getOrderedIds();
        for (long l : ids) {
            res.push(convert(engine.getValue(l)));
        }
        jsValues = res;
    }

    protected JsArray<T> getJsValues() {
        return jsValues;
    }

    protected abstract T convert(V src);

    // Implementation JsListEngineCallback

    @NoExport
    @Override
    public void onItemAddedOrUpdated(V item) {
        updateValues();
    }

    @NoExport
    @Override
    public void onItemRemoved(long id) {
        updateValues();
    }

    @NoExport
    @Override
    public void onClear() {
        updateValues();
    }
}