package im.actor.messenger.app.view;
/*
 * Copyright (C) 2014 darnmason
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.actor.messenger.app.fragment.AnimatorViewHolder;

/**
 * <p>
 * RecyclerView adapter designed to wrap an existing adapter allowing the addition of
 * header views and footer views.
 * </p>
 * <p>
 * I implemented it to aid with the transition from ListView to RecyclerView where the ListView's
 * addHeaderView and addFooterView methods were used. Using this class you may initialize your
 * header views in the Fragment/Activity and add them to the adapter in the same way you used to
 * add them to a ListView.
 * </p>
 * <p>
 * I also required to be able to swap out multiple adapters with different content, therefore
 * setAdapter may be called multiple times.
 * </p>
 * Created by darnmason on 07/11/2014.
 */
public class HeaderViewRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final long HEADERS_ID_START = Long.MIN_VALUE;
    private static final long FOOTERS_ID_END = Long.MAX_VALUE;

    private static final int HEADERS_START = Integer.MIN_VALUE;
    private static final int FOOTERS_START = Integer.MIN_VALUE + 10;
    private static final int ITEMS_START = Integer.MIN_VALUE + 20;
    private static final int ADAPTER_MAX_TYPES = 100;

    private RecyclerView.Adapter mWrappedAdapter;
    private List<View> mHeaderViews, mFooterViews;
    private Map<Class, Integer> mItemTypesOffset;

    /**
     * Construct a new header view recycler adapter
     *
     * @param adapter The underlying adapter to wrap
     */
    public HeaderViewRecyclerAdapter(RecyclerView.Adapter adapter) {
        mHeaderViews = new ArrayList<View>();
        mFooterViews = new ArrayList<View>();
        mItemTypesOffset = new HashMap<Class, Integer>();
        setWrappedAdapter(adapter);
        setHasStableIds(adapter.hasStableIds());
    }

    /**
     * Replaces the underlying adapter, notifying RecyclerView of changes
     *
     * @param adapter The new adapter to wrap
     */
    public void setAdapter(RecyclerView.Adapter adapter) {
        if (mWrappedAdapter != null && mWrappedAdapter.getItemCount() > 0) {
            notifyItemRangeRemoved(getHeaderCount(), mWrappedAdapter.getItemCount());
        }
        setWrappedAdapter(adapter);
        notifyItemRangeInserted(getHeaderCount(), mWrappedAdapter.getItemCount());
    }

    public RecyclerView.Adapter getWrappedAdapter() {
        return mWrappedAdapter;
    }

    @Override
    public long getItemId(int position) {
        int hCount = getHeaderCount();
        if (position < hCount) {
            return HEADERS_ID_START + position;
        } else {
            int itemCount = mWrappedAdapter.getItemCount();
            if (position < hCount + itemCount) {
                return mWrappedAdapter.getItemId(position - hCount);
            } else {
                return FOOTERS_ID_END - (position - hCount - itemCount);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        int hCount = getHeaderCount();
        if (position < hCount) return HEADERS_START + position;
        else {
            int itemCount = mWrappedAdapter.getItemCount();
            if (position < hCount + itemCount) {
                return getAdapterTypeOffset() + mWrappedAdapter.getItemViewType(position - hCount);
            } else return FOOTERS_START + position - hCount - itemCount;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType < HEADERS_START + getHeaderCount()) {
            return new StaticViewHolder(mHeaderViews.get(viewType - HEADERS_START));
        } else if (viewType < FOOTERS_START + getFooterCount()) {
            return new StaticViewHolder(mFooterViews.get(viewType - FOOTERS_START));
        } else {
            return mWrappedAdapter.onCreateViewHolder(viewGroup, viewType - getAdapterTypeOffset());
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int hCount = getHeaderCount();
        if (position >= hCount && position < hCount + mWrappedAdapter.getItemCount())
            mWrappedAdapter.onBindViewHolder(viewHolder, position - hCount);
    }

    /**
     * Add a static view to appear at the start of the RecyclerView. Headers are displayed in the
     * order they were added.
     *
     * @param view The header view to add
     */
    public void addHeaderView(View view) {
        mHeaderViews.add(view);
        notifyDataSetChanged();
    }

    /**
     * Add a static view to appear at the end of the RecyclerView. Footers are displayed in the
     * order they were added.
     *
     * @param view The footer view to add
     */
    public void addFooterView(View view) {
        mFooterViews.add(view);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return getHeaderCount() + getFooterCount() + getWrappedItemCount();
    }

    /**
     * @return The item count in the underlying adapter
     */
    public int getWrappedItemCount() {
        return mWrappedAdapter.getItemCount();
    }

    /**
     * @return The number of header views added
     */
    public int getHeaderCount() {
        return mHeaderViews.size();
    }

    /**
     * @return The number of footer views added
     */
    public int getFooterCount() {
        return mFooterViews.size();
    }

    private void setWrappedAdapter(RecyclerView.Adapter adapter) {
        if (mWrappedAdapter != null) mWrappedAdapter.unregisterAdapterDataObserver(mDataObserver);
        mWrappedAdapter = adapter;
        Class adapterClass = mWrappedAdapter.getClass();
        if (!mItemTypesOffset.containsKey(adapterClass)) putAdapterTypeOffset(adapterClass);
        mWrappedAdapter.registerAdapterDataObserver(mDataObserver);
    }

    private void putAdapterTypeOffset(Class adapterClass) {
        mItemTypesOffset.put(adapterClass, ITEMS_START + mItemTypesOffset.size() * ADAPTER_MAX_TYPES);
    }

    private int getAdapterTypeOffset() {
        return mItemTypesOffset.get(mWrappedAdapter.getClass());
    }

    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
            notifyItemRangeChanged(positionStart + getHeaderCount(), itemCount);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            notifyItemRangeInserted(positionStart + getHeaderCount(), itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            notifyItemRangeRemoved(positionStart + getHeaderCount(), itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            int hCount = getHeaderCount();
            for (int i = 0; i < itemCount; i++) {
                notifyItemMoved(fromPosition + hCount + i, toPosition + hCount + i);
            }
        }
    };

    private static View wrap(View src) {
        if (src.getParent() != null) {
            ((ViewGroup) src.getParent()).removeView(src);
        }
        FrameLayout frameLayout = new FrameLayout(src.getContext());
        frameLayout.setLayoutParams(
                new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        frameLayout.addView(src);
        return frameLayout;
    }

    private static class StaticViewHolder extends AnimatorViewHolder {

        public StaticViewHolder(View itemView) {
            super(wrap(itemView));
        }

        @Override
        public boolean performAnimation() {
            return false;
        }
    }
}