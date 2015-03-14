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

    private final OnItemClickedListener<Dialog> itemClicked;
    private final OnItemClickedListener<Dialog> itemLongClicked;

    public DialogsAdapter(BindedDisplayList<Dialog> displayList, Context context,
                          OnItemClickedListener<Dialog> itemClicked, OnItemClickedListener<Dialog> itemLongClicked) {
        super(displayList);
        this.context = context;
        this.itemClicked = itemClicked;
        this.itemLongClicked = itemLongClicked;
    }

    @Override
    public DialogHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new DialogHolder(context, new FrameLayout(context));
    }

    @Override
    public void onBindViewHolder(DialogHolder dialogHolder, int i) {
        dialogHolder.bind(getItem(i));
    }

    @Override
    public void onViewRecycled(DialogHolder holder) {
        holder.unbind();
    }
}
