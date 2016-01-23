package im.actor.runtime.js.mvvm;

import com.google.gwt.core.client.JavaScriptObject;

import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.Log;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.js.storage.JsListEngine;
import im.actor.runtime.js.storage.JsListEngineCallback;
import im.actor.runtime.js.utils.JsModernArray;
import im.actor.runtime.storage.ListEngineItem;

public class JsDisplayListBind<T extends JavaScriptObject, V extends BserObject & ListEngineItem> implements JsListEngineCallback<V> {

    /**
     * Underlying list engine
     */
    private final JsListEngine<V> listEngine;
    /**
     * Underlying entity converted
     */
    private final JsEntityConverter<V, T> entityConverter;
    /**
     * Convenience flag if overlays supported by entity converter
     */
    private final boolean isOverlaysSupported;


    /**
     * Subscribers to bind
     */
    private final JsDisplayListCallback<T> callback;


    /**
     * Current list values
     */
    private ArrayList<V> values;
    /**
     * Current converted values
     */
    private JsModernArray<T> jsValues;
    /**
     * Current overlay values (if supported)
     */
    private JsModernArray<JavaScriptObject> jsOverlays;
    /**
     * Current dirty overlay items
     */
    private ArrayList<Boolean> isOverlayDirty;

    /**
     * If all messages loaded from top of the list
     */
    private boolean isOpenTop;
    /**
     * If all messages loaded from bottom of the list
     */
    private boolean isOpenBottom;

    /**
     * If list is inited and ready to receive list updates
     */
    private boolean isInited;

    /**
     * If ForceReconvert required flag
     */
    private boolean isForceReconverted = false;

    public JsDisplayListBind(JsDisplayListCallback<T> callback, JsListEngine<V> listEngine, JsEntityConverter<V, T> entityConverter) {

        this.callback = callback;
        this.listEngine = listEngine;
        this.entityConverter = entityConverter;
        this.isOverlaysSupported = entityConverter.isSupportOverlays();

        this.values = new ArrayList<>();
        this.jsValues = JsModernArray.createArray().cast();
        if (isOverlaysSupported) {
            this.isOverlayDirty = new ArrayList<>();
            this.jsOverlays = JsModernArray.createArray().cast();
        }

        isInited = false;

        listEngine.addListener(this);
    }

    public ArrayList<V> getRawItems() {
        return values;
    }

    private void clearState() {
        values.clear();
        jsValues.clear();
        jsOverlays.clear();
        isOverlayDirty.clear();
    }

    public void initAll() {
        clearState();

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

        processDirtyOverlays();

        isInited = true;
        isOpenBottom = true;
        isOpenTop = true;
    }

    public void notifySubscriber() {
        if (isOverlaysSupported) {
            callback.onCollectionChanged(jsValues, jsOverlays);
        } else {
            callback.onCollectionChanged(jsValues, null);
        }
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
                    isOverlayDirty.remove(i);
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
                    isOverlayDirty.add(i, true);
                    markAsDirty(i);
                }
                return;
            }
        }

        values.add(item);
        jsValues.push(entityConverter.convert(item));
        if (isOverlaysSupported) {
            jsOverlays.push(null);
            isOverlayDirty.add(true);
            markAsDirty(values.size() - 1);
        }
    }

    private void remoteItemImpl(long id) {
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).getEngineId() == id) {
                values.remove(i);
                jsValues.remove(i);
                if (isOverlaysSupported) {
                    markAsDirty(i);
                    jsOverlays.remove(i);
                    isOverlayDirty.add(true);
                }
                break;
            }
        }
    }

    /*
     * List Engine Updates
     */

    @Override
    public void onItemAddedOrUpdated(V item) {
        if (!isInited) {
            return;
        }

        addItemOrUpdateImpl(item);
        processDirtyOverlays();
        notifySubscriber();
    }

    @Override
    public void onItemsAddedOrUpdated(List<V> items) {
        if (!isInited) {
            return;
        }

        for (V item : items) {
            addItemOrUpdateImpl(item);
        }
        processDirtyOverlays();
        notifySubscriber();
    }

    @Override
    public void onItemRemoved(long id) {
        if (!isInited) {
            return;
        }
        remoteItemImpl(id);
        processDirtyOverlays();
        notifySubscriber();
    }

    @Override
    public void onItemsRemoved(long[] ids) {
        if (!isInited) {
            return;
        }
        for (long id : ids) {
            remoteItemImpl(id);
        }
        processDirtyOverlays();
        notifySubscriber();
    }

    @Override
    public void onItemsReplaced(List<V> items) {
        if (!isInited) {
            return;
        }

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
        if (!isInited) {
            return;
        }

        values.clear();
        jsValues.clear();
        if (isOverlaysSupported) {
            jsOverlays.clear();
            isOverlayDirty.clear();
        }

        notifySubscriber();
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
            notifySubscriber();
        }
    }

    /*
     * Overlay support methods
     */

    private boolean isDirty(int index) {
        return isOverlayDirty.get(index);
    }

    private void markAsDirty(int index) {
        isOverlayDirty.set(index, true);
        if (index - 1 >= 0) {
            isOverlayDirty.set(index - 1, true);
        }

        if (index + 1 < isOverlayDirty.size()) {
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
}