/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.mvvm;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.actor.runtime.Log;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.js.storage.JsListEngine;
import im.actor.runtime.js.storage.JsListEngineCallback;
import im.actor.runtime.js.utils.JsModernArray;
import im.actor.runtime.mvvm.PlatformDisplayList;
import im.actor.runtime.storage.ListEngineItem;

public class JsDisplayList<T extends JavaScriptObject, V extends BserObject & ListEngineItem> implements
        PlatformDisplayList<V> {

    private final JsListEngine<V> listEngine;
    private final JsEntityConverter<V, T> entityConverter;

    private HashMap<JsDisplayListCallback<T>, JsDisplayListBind<T, V>> binds = new HashMap<>();

    public JsDisplayList(JsListEngine<V> listEngine, JsEntityConverter<V, T> entityConverter) {
        this.listEngine = listEngine;
        this.entityConverter = entityConverter;
    }

    public JsDisplayListBind<T, V> subscribe(JsDisplayListCallback<T> callback, boolean isInverted) {
        if (binds.containsKey(callback)) {
            binds.remove(callback).dispose();
        }
        JsDisplayListBind<T, V> b = new JsDisplayListBind<T, V>(callback, isInverted, listEngine, entityConverter);
        b.initAll();
        binds.put(callback, b);
        return b;
    }

    public void unsubscribe(JsDisplayListCallback<T> callback) {
        if (binds.containsKey(callback)) {
            binds.remove(callback).dispose();
        }
    }

    public JsDisplayListBind<T, V>[] getActiveBinds() {
        return binds.values().toArray(new JsDisplayListBind[0]);
    }

    //
    // Not required methods
    //

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