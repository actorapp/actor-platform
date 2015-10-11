/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.android.view;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import im.actor.runtime.generic.mvvm.AndroidListUpdate;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.runtime.generic.mvvm.ChangeDescription;
import im.actor.runtime.generic.mvvm.DisplayList;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.storage.ListEngineItem;

public abstract class BindedListAdapter<V extends BserObject & ListEngineItem,
        T extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<T> {

    private BindedDisplayList<V> displayList;

    private DisplayList.AndroidChangeListener<V> listener;
    // private DisplayList.Listener listener;

    private AndroidListUpdate<V> currentUpdate = null;

    public BindedListAdapter(BindedDisplayList<V> displayList) {
        this(displayList, true);
    }

    public BindedListAdapter(BindedDisplayList<V> displayList, boolean autoConnect) {
        this.displayList = displayList;
        setHasStableIds(true);

        listener = new DisplayList.AndroidChangeListener<V>() {
            @Override
            public void onCollectionChanged(AndroidListUpdate<V> modification) {
                currentUpdate = modification;
                ChangeDescription<V> currentChange;
                while ((currentChange = modification.next()) != null) {
                    switch (currentChange.getOperationType()) {
                        case ADD:
                            notifyItemRangeInserted(currentChange.getIndex(), currentChange.getLength());
                            break;
                        case UPDATE:
                            notifyItemRangeChanged(currentChange.getIndex(), currentChange.getLength());
                            break;
                        case MOVE:
                            notifyItemMoved(currentChange.getIndex(), currentChange.getDestIndex());
                            break;
                        case REMOVE:
                            notifyItemRangeRemoved(currentChange.getIndex(), currentChange.getLength());
                            break;
                    }
                }
                currentUpdate = null;
            }
        };
        if (autoConnect) {
            resume();
        }
    }

    public boolean isGlobalList() {
        return displayList.isGlobalList();
    }

    public Object getPreprocessedList() {
        return displayList.getProcessedList();
    }

    @Override
    public int getItemCount() {
        if (currentUpdate != null) {
            return currentUpdate.getSize();
        }
        return displayList.getSize();
    }

    protected V getItem(int position) {
        if (currentUpdate != null) {
            return currentUpdate.getItem(position);
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
        displayList.addAndroidListener(listener);
        notifyDataSetChanged();
    }

    public void pause() {
        displayList.removeAndroidListener(listener);
    }

    public void dispose() {
        pause();
    }
}
