package im.actor.messenger.app.fragment.main;

import android.content.Context;
import android.view.ViewGroup;

import im.actor.core.entity.SearchEntity;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.messenger.app.view.OnItemClickedListener;
import im.actor.runtime.android.view.BindedListAdapter;

/**
 * Created by ex3ndr on 05.04.15.
 */
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
    public SearchHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new SearchHolder(context, onItemClickedListener);
    }

    @Override
    public void onBindViewHolder(SearchHolder dialogHolder, int index, SearchEntity item) {
        dialogHolder.bind(item, query, index == getItemCount() - 1);
    }
}
