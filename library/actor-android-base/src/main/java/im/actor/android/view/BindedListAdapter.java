/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.android.view;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.engine.ListEngineItem;
import im.actor.model.mvvm.BindedDisplayList;
import im.actor.model.mvvm.DefferedListChange;
import im.actor.model.mvvm.DefferedListModification;
import im.actor.model.mvvm.DisplayList;

public abstract class BindedListAdapter<V extends BserObject & ListEngineItem,
        T extends BindedViewHolder>
        extends RecyclerView.Adapter<T> {

    private BindedDisplayList<V> displayList;

    private DisplayList.DifferedChangeListener<V> listener;
    // private DisplayList.Listener listener;

    private DefferedListChange<V> currentChange = null;

    public BindedListAdapter(BindedDisplayList<V> displayList) {
        this(displayList, true);
    }

    public BindedListAdapter(BindedDisplayList<V> displayList, boolean autoConnect) {
        this.displayList = displayList;
        setHasStableIds(false);

        listener = new DisplayList.DifferedChangeListener<V>() {
            @Override
            public void onCollectionChanged(DefferedListChange<V> modification) {
                startUpdates(modification);
                DefferedListModification<V> currentChange;
                while ((currentChange = modification.next()) != null) {
                    switch (currentChange.getOperation()) {
                        case ADD:
                            notifyItemInserted(currentChange.getIndex());
                            break;
                        case ADD_RANGE:
                            notifyItemRangeInserted(currentChange.getIndex(), currentChange.getLength());
                            break;
                        case UPDATE:
                            notifyItemChanged(currentChange.getIndex());
                            break;
                        case UPDATE_RANGE:
                            notifyItemRangeChanged(currentChange.getIndex(), currentChange.getLength());
                            break;
                        case REMOVE:
                            notifyItemRemoved(currentChange.getIndex());
                            break;
                        case REMOVE_RANGE:
                            notifyItemRangeRemoved(currentChange.getIndex(), currentChange.getDestIndex());
                            break;
                        case MOVE:
                            notifyItemMoved(currentChange.getIndex(), currentChange.getDestIndex());
                            notifyItemChanged(currentChange.getIndex());
                            break;
                    }
                }
                stopUpdates();
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
        displayList.addDifferedListener(listener);
        notifyDataSetChanged();
    }

    public void pause() {
        displayList.removeDifferedListener(listener);
    }

    public void dispose() {
        pause();
    }
}
