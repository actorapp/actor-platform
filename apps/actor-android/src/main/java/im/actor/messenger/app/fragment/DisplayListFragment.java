package im.actor.messenger.app.fragment;

import android.app.Activity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.ChatLinearLayoutManager;
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
import im.actor.model.mvvm.BindedDisplayList;
import im.actor.model.mvvm.DisplayList;

/**
 * Created by ex3ndr on 15.03.15.
 */
public abstract class DisplayListFragment<T extends BserObject & ListEngineItem,
        V extends BindedViewHolder> extends BaseFragment implements DisplayList.Listener {

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
                new ChatLinearLayoutManager(getActivity(), ChatLinearLayoutManager.VERTICAL, false));
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
        displayList.addListener(this);
        onCollectionChanged();
    }

    @Override
    public void onCollectionChanged() {
        if (displayList.getSize() == 0) {
            hideView(collection);
        } else {
            showView(collection);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        adapter.pause();
        displayList.removeListener(this);
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
