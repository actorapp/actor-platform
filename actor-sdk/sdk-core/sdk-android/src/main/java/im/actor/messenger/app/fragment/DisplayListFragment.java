package im.actor.messenger.app.fragment;

import android.app.Activity;
import android.support.v7.widget.ChatLinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.runtime.generic.mvvm.DisplayList;
import im.actor.messenger.R;
import im.actor.messenger.app.view.HeaderViewRecyclerAdapter;
import im.actor.runtime.android.view.BindedListAdapter;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.storage.ListEngineItem;

public abstract class DisplayListFragment<T extends BserObject & ListEngineItem,
        V extends RecyclerView.ViewHolder> extends BaseFragment implements DisplayList.Listener {

    private RecyclerView collection;
    // private View emptyCollection;

    private BindedDisplayList<T> displayList;
    private BindedListAdapter<T, V> adapter;

    protected View inflate(LayoutInflater inflater, ViewGroup container, int resource, BindedDisplayList<T> displayList) {
        View res = inflater.inflate(resource, container, false);
        afterViewInflate(res, displayList);
        return res;
    }

    protected void afterViewInflate(View view, BindedDisplayList<T> displayList) {
        collection = (RecyclerView) view.findViewById(R.id.collection);
        if (displayList.getSize() == 0) {
            collection.setVisibility(View.INVISIBLE);
        } else {
            collection.setVisibility(View.VISIBLE);
        }
        setAnimationsEnabled(true);

        this.displayList = displayList;
        configureRecyclerView(collection);

        // emptyCollection = res.findViewById(R.id.emptyCollection);

        adapter = onCreateAdapter(displayList, getActivity());

        collection.setAdapter(adapter);

//        if (emptyCollection != null) {
//            emptyCollection.setVisibility(View.GONE);
//        }
    }

    public void setAnimationsEnabled(boolean isEnabled) {
        if (isEnabled) {
            DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
            itemAnimator.setSupportsChangeAnimations(false);
            itemAnimator.setMoveDuration(200);
            itemAnimator.setAddDuration(150);
            itemAnimator.setRemoveDuration(200);
            collection.setItemAnimator(itemAnimator);
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
        if (displayList.getSize() == 0) {
            hideView(collection, false);
        } else {
            showView(collection, false);
        }
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
