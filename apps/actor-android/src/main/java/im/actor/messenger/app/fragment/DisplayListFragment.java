package im.actor.messenger.app.fragment;

import android.app.Activity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import im.actor.messenger.R;
import im.actor.messenger.app.view.HeaderViewRecyclerAdapter;
import im.actor.android.view.BindedListAdapter;
import im.actor.android.view.BindedViewHolder;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.engine.ListEngineItem;
import im.actor.model.mvvm.DefferedListChange;
import im.actor.model.mvvm.DefferedListModification;
import im.actor.model.mvvm.BindedDisplayList;
import im.actor.model.mvvm.DisplayList;

/**
 * Created by ex3ndr on 15.03.15.
 */
public abstract class DisplayListFragment<T extends BserObject & ListEngineItem,
        V extends BindedViewHolder> extends BaseFragment implements DisplayList.DifferedChangeListener<T> {

    private RecyclerView collection;
    // private View emptyCollection;

    private BindedDisplayList<T> displayList;
    private BindedListAdapter<T, V> adapter;

    protected View inflate(LayoutInflater inflater, ViewGroup container, int resource, BindedDisplayList<T> displayList) {
        View res = inflater.inflate(resource, container, false);
        collection = (RecyclerView) res.findViewById(R.id.collection);
        if (displayList.getSize() == 0) {
            collection.setVisibility(View.INVISIBLE);
        } else {
            collection.setVisibility(View.VISIBLE);
        }
        configureRecyclerView(collection);

        // emptyCollection = res.findViewById(R.id.emptyCollection);

        this.displayList = displayList;
        adapter = onCreateAdapter(displayList, getActivity());

        collection.setAdapter(adapter);

//        if (emptyCollection != null) {
//            emptyCollection.setVisibility(View.GONE);
//        }

        return res;
    }

    public void setAnimationsEnabled(boolean isEnabled) {
        if (isEnabled) {
            collection.setItemAnimator(new DefaultItemAnimator());
        } else {
            collection.setItemAnimator(null);
        }
    }

    protected void configureRecyclerView(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }

    protected void addHeaderView(View header) {
        if (collection.getAdapter() instanceof HeaderViewRecyclerAdapter) {
            HeaderViewRecyclerAdapter h = (HeaderViewRecyclerAdapter) collection.getAdapter();
            h.addHeaderView(header);
        } else {
            HeaderViewRecyclerAdapter headerViewRecyclerAdapter = new HeaderViewRecyclerAdapter(adapter);
            headerViewRecyclerAdapter.addHeaderView(header);
            collection.setAdapter(headerViewRecyclerAdapter);
        }
    }

    protected void addFooterView(View header) {
        if (collection.getAdapter() instanceof HeaderViewRecyclerAdapter) {
            HeaderViewRecyclerAdapter h = (HeaderViewRecyclerAdapter) collection.getAdapter();
            h.addFooterView(header);
        } else {
            HeaderViewRecyclerAdapter headerViewRecyclerAdapter = new HeaderViewRecyclerAdapter(adapter);
            headerViewRecyclerAdapter.addFooterView(header);
            collection.setAdapter(headerViewRecyclerAdapter);
        }
    }

    protected abstract BindedListAdapter<T, V> onCreateAdapter(BindedDisplayList<T> displayList, Activity activity);

    public BindedListAdapter<T, V> getAdapter() {
        return adapter;
    }

    public BindedDisplayList<T> getDisplayList() {
        return displayList;
    }

    public RecyclerView getCollection() {
        return collection;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.resume();
        displayList.addDifferedListener(this);
        if (displayList.getSize() == 0) {
            hideView(collection, false);
        } else {
            showView(collection, false);
        }
        // onCollectionChanged();
    }

    @Override
    public void onCollectionChanged(DefferedListChange<T> modification) {
        adapter.startUpdates(modification);
        DefferedListModification<T> currentChange;
        while ((currentChange = modification.next()) != null) {
            switch (currentChange.getOperation()) {
                case ADD:
                    adapter.notifyItemInserted(currentChange.getIndex());
                    break;
                case ADD_RANGE:
                    adapter.notifyItemRangeInserted(currentChange.getIndex(), currentChange.getLength());
                    break;
                case UPDATE:
                    adapter.notifyItemChanged(currentChange.getIndex());
                    break;
                case UPDATE_RANGE:
                    adapter.notifyItemRangeChanged(currentChange.getIndex(), currentChange.getLength());
                    break;
                case REMOVE:
                    adapter.notifyItemRemoved(currentChange.getIndex());
                    break;
                case REMOVE_RANGE:
                    adapter.notifyItemRangeRemoved(currentChange.getIndex(), currentChange.getDestIndex());
                    break;
                case MOVE:
                    adapter.notifyItemMoved(currentChange.getIndex(), currentChange.getDestIndex());
                    break;
            }
        }
        adapter.stopUpdates();

        if (displayList.getSize() == 0) {
            hideView(collection);
        } else {
            showView(collection);
        }
    }

//    @Override
//    public void onCollectionChanged() {
//        if (displayList.getSize() == 0) {
//            hideView(collection);
//        } else {
//            showView(collection);
//        }
//    }

    @Override
    public void onPause() {
        super.onPause();
        adapter.pause();
        displayList.removeDifferedListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (adapter != null) {
            if (!adapter.isGlobalList()) {
                adapter.dispose();
            }
            adapter = null;
        }

        // emptyCollection = null;
        collection = null;
    }
}
