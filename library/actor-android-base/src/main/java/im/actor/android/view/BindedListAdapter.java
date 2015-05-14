/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.android.view;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.engine.ListEngineItem;
import im.actor.model.mvvm.DefferedListChange;
import im.actor.model.mvvm.BindedDisplayList;
import im.actor.model.mvvm.DisplayList;

public abstract class BindedListAdapter<V extends BserObject & ListEngineItem,
        T extends BindedViewHolder>
        extends RecyclerView.Adapter<T> {

    private BindedDisplayList<V> displayList;

    private DisplayList.Listener listener;

    private DefferedListChange<V> currentChange = null;

    public BindedListAdapter(BindedDisplayList<V> displayList) {
        this(displayList, true);
    }

    public BindedListAdapter(BindedDisplayList<V> displayList, boolean autoConnect) {
        this.displayList = displayList;
        setHasStableIds(true);
        listener = new DisplayList.Listener() {
            @Override
            public void onCollectionChanged() {
                notifyDataSetChanged();
            }
        };

        if (autoConnect) {
            resume();
        }
    }

    public boolean isGlobalList() {
        return displayList.isGlobalList();
    }

    public void startUpdates(DefferedListChange<V> currentChange) {
        this.currentChange = currentChange;
    }

    public void stopUpdates() {
        this.currentChange = null;
    }

    @Override
    public int getItemCount() {
        if (currentChange != null) {
            return currentChange.getCount();
        }
        return displayList.getSize();
    }

    protected V getItem(int position) {
        if (currentChange != null) {
            return currentChange.getItem(position);
        }
        return displayList.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getEngineId();
    }

    @Override
    public abstract T onCreateViewHolder(ViewGroup viewGroup, int viewType);

    @Override
    public final void onBindViewHolder(T dialogHolder, int i) {
        displayList.touch(i);
        onBindViewHolder(dialogHolder, i, getItem(i));
    }

    public abstract void onBindViewHolder(T dialogHolder, int index, V item);


    public void resume() {
        displayList.addListener(listener);
        notifyDataSetChanged();
    }

    public void pause() {
        displayList.removeListener(listener);
    }

    public void dispose() {
        pause();
    }
}
