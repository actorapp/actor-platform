package com.droidkit.engine.list.view;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.droidkit.engine.uilist.UiList;
import com.droidkit.engine.uilist.UiListListener;
import im.actor.messenger.util.Logger;

/**
 * Created by ex3ndr on 30.08.14.
 */
public abstract class ListEngineAdapter<V> extends BaseAdapter {

    private static final int DEFAULT_LOAD_GAP = 10;

    private EngineUiList<V> engine;
    private UiList<V> uiList;
    private UiListListener listListener;
    private final int LOAD_GAP;
    private final boolean stackFromBottom;
    private final boolean autoUpdate;

    public ListEngineAdapter(EngineUiList<V> engine) {
        this(engine, false);
    }

    public ListEngineAdapter(EngineUiList<V> engine, boolean stackFromBottom) {
        this(engine, stackFromBottom, DEFAULT_LOAD_GAP, true);
    }

    public ListEngineAdapter(EngineUiList<V> engine, boolean stackFromBottom, boolean autoLoad) {
        this(engine, stackFromBottom, DEFAULT_LOAD_GAP, autoLoad);
    }

    public ListEngineAdapter(EngineUiList<V> engine, boolean stackFromBottom, int loadGap, boolean autoUpdate) {
        this.engine = engine;
        this.uiList = engine.getUiList();
        this.LOAD_GAP = loadGap;
        this.stackFromBottom = stackFromBottom;
        this.autoUpdate = autoUpdate;
        this.listListener = new UiListListener() {
            @Override
            public void onListUpdated() {
                notifyDataSetChanged();
            }
        };
        resume();
    }

    public EngineUiList<V> getEngine() {
        return engine;
    }

    public UiList<V> getUiList() {
        return uiList;
    }

    @Override
    public final int getCount() {
        return uiList.getSize();
    }

    @Override
    public final long getItemId(int position) {
        if (stackFromBottom) {
            return getItemId(getItem(getCount() - position - 1));
        } else {
            return getItemId(getItem(position));
        }
    }

    public abstract long getItemId(V obj);

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public final V getItem(int position) {
        if (stackFromBottom) {
            return uiList.getItem(uiList.getSize() - position - 1);
        } else {
            return uiList.getItem(position);
        }
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        V item = getItem(position);
        if (stackFromBottom) {
            if (position < LOAD_GAP) {
                engine.requestLoadTail();
            } else if (position > getCount() - LOAD_GAP) {
                // Logger.d("ListEngine", "Loading head #" + position);
                engine.requestLoadHead();
            }
        } else {
            if (position > getCount() - LOAD_GAP) {
                // Logger.d("ListEngine", "Loading tail #" + position);
                engine.requestLoadTail();
            } else if (position < LOAD_GAP) {
                engine.requestLoadHead();
            }
        }
        return getView(item, position, convertView, parent);
    }

    public abstract View getView(V object, int position, View convertView, ViewGroup parent);

    public void pause() {
        if (autoUpdate) {
            uiList.removeListener(listListener);
        }
    }

    public void resume() {
        if (autoUpdate) {
            uiList.addListener(listListener);
        }
        notifyDataSetChanged();
    }

    public void dispose() {
        pause();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        // Logger.d("ListEngine", "notifyDataSetChanged");
    }
}
