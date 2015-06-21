/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.android.view;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.engine.ListEngineItem;
import im.actor.model.mvvm.AndroidListUpdate;
import im.actor.model.mvvm.BindedDisplayList;
import im.actor.model.mvvm.ChangeDescription;
import im.actor.model.mvvm.DisplayList;

public abstract class BindedListAdapter<V extends BserObject & ListEngineItem,
        T extends BindedViewHolder>
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
