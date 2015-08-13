package im.actor.messenger.app.fragment.dialogs;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import im.actor.core.entity.Dialog;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.messenger.app.view.OnItemClickedListener;
import im.actor.runtime.android.view.BindedListAdapter;

public class DialogsAdapter extends BindedListAdapter<Dialog, DialogHolder> {

    private OnItemClickedListener<Dialog> onItemClicked;
    private Context context;

    public DialogsAdapter(BindedDisplayList<Dialog> displayList, OnItemClickedListener<Dialog> onItemClicked,
                          Context context) {
        super(displayList);
        this.context = context;
        this.onItemClicked = onItemClicked;
    }

    @Override
    public DialogHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new DialogHolder(context, new FrameLayout(context), onItemClicked);
    }

    @Override
    public void onBindViewHolder(DialogHolder dialogHolder, int index, Dialog item) {
        dialogHolder.bind(item, index == getItemCount() - 1);
    }

    @Override
    public void onViewRecycled(DialogHolder holder) {
        holder.unbind();
    }
}
