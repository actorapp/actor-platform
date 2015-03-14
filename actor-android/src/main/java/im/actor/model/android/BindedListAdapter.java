package im.actor.model.android;

import android.support.v7.widget.RecyclerView;

import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.engine.ListEngineItem;
import im.actor.model.mvvm.BindedDisplayList;
import im.actor.model.mvvm.DisplayList;

/**
 * Created by ex3ndr on 14.03.15.
 */
public abstract class BindedListAdapter<V extends BserObject & ListEngineItem,
        T extends android.support.v7.widget.RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<T> {

    private BindedDisplayList<V> displayList;

    private DisplayList.Listener listener;

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

    @Override
    public int getItemCount() {
        return displayList.getSize();
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getEngineId();
    }

    protected V getItem(int position) {
        return displayList.getItem(position);
    }

    public void pause() {
        displayList.removeListener(listener);
    }

    public void resume() {
        displayList.addListener(listener);
    }
}
