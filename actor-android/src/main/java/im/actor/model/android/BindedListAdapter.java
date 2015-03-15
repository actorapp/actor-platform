package im.actor.model.android;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

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

    @Override
    public final T onCreateViewHolder(ViewGroup viewGroup, int i) {
        return onCreateViewHolder(viewGroup, i, getItem(i));
    }

    @Override
    public final void onBindViewHolder(T dialogHolder, int i) {
        if (i > getItemCount() - 20) {
            displayList.loadMoreForward();
        }
        if (i < 20) {
            displayList.loadMoreBackward();
        }

        onBindViewHolder(dialogHolder, i, getItem(i));
    }

    public abstract void onBindViewHolder(T dialogHolder, int index, V item);

    public abstract T onCreateViewHolder(ViewGroup viewGroup, int index, V item);

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
