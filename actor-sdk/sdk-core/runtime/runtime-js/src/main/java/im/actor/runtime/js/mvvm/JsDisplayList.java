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
import im.actor.runtime.js.utils.JsModernArray;
import im.actor.runtime.mvvm.PlatformDisplayList;
import im.actor.runtime.storage.ListEngineItem;

public class JsDisplayList<T extends JavaScriptObject, V extends BserObject & ListEngineItem> implements JsListEngineCallback<V>,
        PlatformDisplayList<V> {

    private final JsListEngine<V> listEngine;
    private final JsEntityConverter<V, T> entityConverter;

    private ArrayList<JsDisplayListCallback<T>> callbacks = new ArrayList<JsDisplayListCallback<T>>();
    private ArrayList<JsDisplayListCallback<T>> callbacksInverted = new ArrayList<JsDisplayListCallback<T>>();

    private ArrayList<V> values;
    private ArrayList<Boolean> isOverlayDirty;
    private JsModernArray<T> jsValues;
    private JsModernArray<JavaScriptObject> jsOverlays;
    private boolean isForceReconverted = false;
    private final boolean isOverlaysSupported;

    public JsDisplayList(JsListEngine<V> listEngine, JsEntityConverter<V, T> entityConverter) {
        this.listEngine = listEngine;
        this.entityConverter = entityConverter;
        this.isOverlaysSupported = entityConverter.isSupportOverlays();

        this.values = new ArrayList<V>();
        this.jsValues = JavaScriptObject.createArray().cast();

        if (isOverlaysSupported) {
            this.isOverlayDirty = new ArrayList<Boolean>();
            this.jsOverlays = JavaScriptObject.createArray().cast();
        }

        //
        // Building initial list
        //

        long[] rids = listEngine.getOrderedIds();
        for (long rid : rids) {
            V item = listEngine.getValue(rid);
            if (item == null) {
                Log.w("JsDisplayList", "Unable to find item #" + rid);
                continue;
            }
            values.add(item);
            jsValues.push(entityConverter.convert(item));

            if (isOverlaysSupported) {
                jsOverlays.push(null);
                isOverlayDirty.add(true);
            }
        }

        //
        // Processing dirty overlays
        //
        processDirtyOverlays();

        //
        // Subscribing to updates
        //

        listEngine.addListener(this);
    }

    public ArrayList<V> getRawItems() {
        return values;
    }

    @Override
    public void onItemAddedOrUpdated(V item) {
        addItemOrUpdateImpl(item);
        processDirtyOverlays();
        notifySubscribers();
    }

    @Override
    public void onItemsAddedOrUpdated(List<V> items) {
        for (V item : items) {
            addItemOrUpdateImpl(item);
        }
        processDirtyOverlays();
        notifySubscribers();
    }

    private void addItemOrUpdateImpl(V item) {
        long id = item.getEngineId();
        long sortKey = item.getEngineSort();
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).getEngineId() == id) {
                values.remove(i);
                jsValues.remove(i);
                if (isOverlaysSupported) {
                    markAsDirty(i);
                    jsOverlays.remove(i);
                }
                break;
            }
        }

        for (int i = 0; i < values.size(); i++) {
            if (sortKey > values.get(i).getEngineSort()) {
                values.add(i, item);
                jsValues.insert(i, entityConverter.convert(item));
                if (isOverlaysSupported) {
                    jsOverlays.insert(i, null);
                    markAsDirty(i);
                }
                return;
            }
        }

        values.add(item);
        jsValues.push(entityConverter.convert(item));
        if (isOverlaysSupported) {
            jsOverlays.push(null);
            markAsDirty(values.size() - 1);
        }
    }

    @Override
    public void onItemRemoved(long id) {
        remoteItemImpl(id);
        processDirtyOverlays();
        notifySubscribers();
    }

    @Override
    public void onItemsRemoved(long[] ids) {
        for (long id : ids) {
            remoteItemImpl(id);
        }
        processDirtyOverlays();
        notifySubscribers();
    }

    private void remoteItemImpl(long id) {
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).getEngineId() == id) {
                values.remove(i);
                jsValues.remove(i);
                if (isOverlaysSupported) {
                    markAsDirty(i);
                    jsOverlays.remove(i);
                }
                break;
            }
        }
    }

    @Override
    public void onItemsReplaced(List<V> items) {
        values.clear();
        jsValues.clear();
        if (isOverlaysSupported) {
            jsOverlays.clear();
            isOverlayDirty.clear();
        }
        onItemsAddedOrUpdated(items);
    }

    @Override
    public void onClear() {
        values.clear();
        jsValues.clear();
        if (isOverlaysSupported) {
            jsOverlays.clear();
            isOverlayDirty.clear();
        }
        notifySubscribers();
    }

    //
    // Notifications
    //

    public void subscribe(JsDisplayListCallback<T> callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }

        if (isOverlaysSupported) {
            callback.onCollectionChanged(jsValues, jsOverlays);
        } else {
            callback.onCollectionChanged(jsValues, null);
        }
    }

    public void unsubscribe(JsDisplayListCallback<T> callback) {
        callbacks.remove(callback);
    }

    public void subscribeInverted(JsDisplayListCallback<T> callback) {
        if (!callbacksInverted.contains(callback)) {
            callbacksInverted.add(callback);
        }

        if (isOverlaysSupported) {
            callback.onCollectionChanged(jsValues.reverse(), jsOverlays.reverse());
        } else {
            callback.onCollectionChanged(jsValues.reverse(), null);
        }
    }

    public void unsubscribeInverted(JsDisplayListCallback<T> callback) {
        callbacksInverted.remove(callback);
    }

    private void notifySubscribers() {
        if (isOverlaysSupported) {
            for (JsDisplayListCallback<T> callback : callbacks) {
                callback.onCollectionChanged(jsValues, jsOverlays);
            }
        } else {
            for (JsDisplayListCallback<T> callback : callbacks) {
                callback.onCollectionChanged(jsValues, null);
            }
        }

        if (callbacksInverted.size() > 0) {
            JsArray<T> rev = jsValues.reverse();
            if (isOverlaysSupported) {
                JsArray<JavaScriptObject> revOverlays = jsOverlays.reverse();
                for (JsDisplayListCallback<T> callback : callbacksInverted) {
                    callback.onCollectionChanged(rev, revOverlays);
                }
            } else {
                for (JsDisplayListCallback<T> callback : callbacksInverted) {
                    callback.onCollectionChanged(rev, null);
                }
            }
        }
    }

    //
    // Reconverting
    //

    public void startReconverting() {
        isForceReconverted = false;
    }

    public void forceReconvert(long id) {
        for (int i = 0; i < values.size(); i++) {
            V value = values.get(i);
            if (value.getEngineId() == id) {
                jsValues.update(i, entityConverter.convert(value));
                // Do not update overlays as this is method is a hack for binding
                isForceReconverted = true;
                break;
            }
        }
    }

    public void stopReconverting() {
        if (isForceReconverted) {
            isForceReconverted = false;
            notifySubscribers();
        }
    }

    //
    // Overlays
    //

    private boolean isDirty(int index) {
        return isOverlayDirty.get(index);
    }

    private void markAsDirty(int index) {
        isOverlayDirty.set(index, true);
        if (index - 1 > 0) {
            isOverlayDirty.set(index - 1, true);
        }

        if (index < isOverlayDirty.size()) {
            isOverlayDirty.set(index + 1, true);
        }
    }

    private void markAsClean(int index) {
        isOverlayDirty.set(index, false);
    }

    private boolean processDirtyOverlays() {
        if (!isOverlaysSupported) {
            return false;
        }
        boolean isChanged = false;
        for (int i = 0; i < values.size(); i++) {
            if (!isDirty(i)) {
                continue;
            }

            V prev = null;
            V current = values.get(i);
            V next = null;

            if (i - 1 >= 0) {
                prev = values.get(i - 1);
            }
            if (i + 1 < values.size()) {
                next = values.get(i + 1);
            }

            jsOverlays.update(i, entityConverter.buildOverlay(prev, current, next));
            markAsClean(i);
            isChanged = true;
        }
        return isChanged;
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