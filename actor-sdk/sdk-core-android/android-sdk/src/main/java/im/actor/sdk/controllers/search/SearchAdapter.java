package im.actor.sdk.controllers.search;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;

import im.actor.core.entity.SearchEntity;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.adapters.OnItemClickedListener;
import im.actor.runtime.android.view.BindedListAdapter;

public class SearchAdapter extends BindedListAdapter<SearchEntity, SearchHolder> {

    private Context context;
    private String query;
    private OnItemClickedListener<SearchEntity> onItemClickedListener;

    public SearchAdapter(Context context, BindedDisplayList<SearchEntity> displayList,
                         OnItemClickedListener<SearchEntity> onItemClickedListener) {
        super(displayList);
        this.context = context;
        this.onItemClickedListener = onItemClickedListener;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public int getItemViewType(int position) {
        SearchEntity e = getItem(position);
        if (e instanceof GlobalSearchBaseFragment.SearchEntityHeader) {
            return 1;
        } else {
            return super.getItemViewType(position);
        }
    }

    @Override
    public SearchHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case 1:
                return new SearchHolderEx(context, onItemClickedListener);
            default:
            case 0:
                return new SearchHolder(context, onItemClickedListener);
        }
    }

    @Override
    public void onBindViewHolder(SearchHolder dialogHolder, int index, SearchEntity item) {
        dialogHolder.bind(item, query, index == getItemCount() - 1);
    }

    public class SearchHolderEx extends SearchHolder {
        public SearchHolderEx(Context context, OnItemClickedListener<SearchEntity> clickedListener) {
            super(context, clickedListener);
        }

        @Override
        protected void init(Context context, OnItemClickedListener<SearchEntity> clickedListener) {
            itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            itemView.setBackgroundColor(ActorSDK.sharedActor().style.getBackyardBackgroundColor());
            TextView globalSearchTitle = new TextView(context);
            globalSearchTitle.setText(R.string.main_search_global_header);
            globalSearchTitle.setTextSize(16);
            globalSearchTitle.setPadding(Screen.dp(12), Screen.dp(8), 0, Screen.dp(8));
            globalSearchTitle.setBackgroundColor(ActorSDK.sharedActor().style.getBackyardBackgroundColor());
            globalSearchTitle.setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
            ((ViewGroup) itemView).addView(globalSearchTitle);
        }

        @Override
        public void bind(SearchEntity entity, String query, boolean isLast) {
        }
    }
}
