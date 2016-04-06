package im.actor.allmessages;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import im.actor.runtime.android.view.BindedListAdapter;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.sdk.view.adapters.OnItemClickedListener;

public class AllMessagesAdapter extends BindedListAdapter<MessageEx, AllMessageHolder> {

    private OnItemClickedListener<MessageEx> onItemClicked;
    private Context context;

    public AllMessagesAdapter(BindedDisplayList<MessageEx> displayList, OnItemClickedListener<MessageEx> onItemClicked,
                              Context context) {
        super(displayList);
        this.context = context;
        this.onItemClicked = onItemClicked;
    }

    @Override
    public AllMessageHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new AllMessageHolder(new FrameLayout(context), context, onItemClicked);
    }

    @Override
    public void onBindViewHolder(AllMessageHolder messageHolder, int index, MessageEx item) {
        messageHolder.bind(item, index == getItemCount() - 1);
    }

    @Override
    public void onViewRecycled(AllMessageHolder holder) {
        holder.unbind();
    }
}
