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

    private ArrayList<SubscriberHolder> binds = new ArrayList<SubscriberHolder>();

    public JsDisplayList(JsListEngine<V> listEngine, JsEntityConverter<V, T> entityConverter) {
        this.listEngine = listEngine;
        this.entityConverter = entityConverter;
    }

    public JsDisplayListBind<T, V> subscribe(JsDisplayListCallback<T> callback, boolean isInverted) {
        unbind(callback);
        JsDisplayListBind<T, V> b = new JsDisplayListBind<T, V>(callback, isInverted, listEngine, entityConverter);
        b.initAll();
        binds.add(new SubscriberHolder(callback, b));
        return b;
    }

    public void unsubscribe(JsDisplayListCallback<T> callback) {
        unbind(callback);
    }

    private void unbind(JsDisplayListCallback<T> callback) {
        for (SubscriberHolder s : binds) {
            if (s.callback.equals(callback)) {
                s.bind.dispose();
                binds.remove(s);
                break; // Can be only one
            }
        }
    }

    public JsDisplayListBind<T, V>[] getActiveBinds() {
        JsDisplayListBind[] res = new JsDisplayListBind[binds.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = binds.get(i).bind;
        }
        return res;
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

    /**
     * Work-around for impossibility of using of hash maps for closures
     */
    private class SubscriberHolder {

        private JsDisplayListCallback<T> callback;
        private JsDisplayListBind<T, V> bind;

        public SubscriberHolder(JsDisplayListCallback<T> callback, JsDisplayListBind<T, V> bind) {
            this.callback = callback;
            this.bind = bind;
        }

        public JsDisplayListCallback<T> getCallback() {
            return callback;
        }

        public JsDisplayListBind<T, V> getBind() {
            return bind;
        }
    }
}