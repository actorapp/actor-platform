package im.actor.messenger.app.fragment.dialogs;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import im.actor.android.view.BindedListAdapter;
import im.actor.messenger.app.view.OnItemClickedListener;
import im.actor.model.entity.Dialog;
import im.actor.model.mvvm.BindedDisplayList;

public class DialogsAdapter extends BindedListAdapter<Dialog, DialogHolder> {

    private final Context context;

    private OnItemClickedListener<Dialog> itemClicked;

    public DialogsAdapter(BindedDisplayList<Dialog> displayList,
                          OnItemClickedListener<Dialog> itemClicked, Context context) {
        super(displayList);
        this.itemClicked = itemClicked;
        this.context = context;
    }

    @Override
    public DialogHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new DialogHolder(context, new FrameLayout(context), itemClicked);
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
