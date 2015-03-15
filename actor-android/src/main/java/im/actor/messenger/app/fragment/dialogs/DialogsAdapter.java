package im.actor.messenger.app.fragment.dialogs;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import im.actor.messenger.app.view.OnItemClickedListener;
import im.actor.model.android.BindedListAdapter;
import im.actor.model.entity.Dialog;
import im.actor.model.mvvm.BindedDisplayList;

public class DialogsAdapter extends BindedListAdapter<Dialog, DialogHolder> {

    private final Context context;

    private OnItemClickedListener<Dialog> itemClicked;
    private OnItemClickedListener<Dialog> itemLongClicked;

    public DialogsAdapter(BindedDisplayList<Dialog> displayList, Context context) {
        super(displayList);
        this.context = context;
    }

    public void setItemClicked(OnItemClickedListener<Dialog> itemClicked) {
        this.itemClicked = itemClicked;
    }

    public void setItemLongClicked(OnItemClickedListener<Dialog> itemLongClicked) {
        this.itemLongClicked = itemLongClicked;
    }

    @Override
    public DialogHolder onCreateViewHolder(ViewGroup viewGroup, int index, Dialog item) {
        return new DialogHolder(context, new FrameLayout(context), itemClicked, itemLongClicked);
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
