package im.actor.sdk.controllers.dialogs.view;

import im.actor.core.entity.Dialog;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.view.adapters.OnItemClickedListener;
import im.actor.runtime.android.view.BindedViewHolder;

public class DialogHolder extends BindedViewHolder {

    protected ActorStyle style = ActorSDK.sharedActor().style;

    private Dialog bindedItem;

    private DialogView dialogView;

    public DialogHolder(DialogView dialogView, final OnItemClickedListener<Dialog> onClickListener) {
        super(dialogView);

        this.dialogView = dialogView;

        dialogView.setOnClickListener(v -> {
            if (bindedItem != null) {
                onClickListener.onClicked(bindedItem);
            }
        });
        dialogView.setOnLongClickListener(v -> {
            if (bindedItem != null) {
                return onClickListener.onLongClicked(bindedItem);
            }
            return false;
        });
    }

    public void bind(Dialog data, boolean isLast) {
        this.bindedItem = data;
        this.dialogView.bind(data);
        this.dialogView.setDividerVisible(!isLast);
    }

    public void unbind() {
        this.bindedItem = null;
        this.dialogView.unbind();
    }
}