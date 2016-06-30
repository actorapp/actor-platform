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
//
//        pendingColor = style.getDialogsStatePendingColor();
//        sentColor = style.getDialogsStateSentColor();
//        receivedColor = style.getDialogsStateDeliveredColor();
//        readColor = style.getDialogsStateReadColor();
//        errorColor = style.getDialogsStateErrorColor();

//        state = new TintImageView(context);
//        {
//            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(Screen.dp(28), Screen.dp(12), Gravity.BOTTOM | Gravity.RIGHT);
//            params.bottomMargin = Screen.dp(16);
//            params.rightMargin = Screen.dp(9);
//            state.setLayoutParams(params);
//            fl.addView(state);
//        }


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

//        if (data.getSenderId() != myUid() || data.getUnreadCount() > 0) {
//            state.setVisibility(View.GONE);
//        } else {
//            if (data.isRead()) {
//                state.setResource(R.drawable.msg_check_2);
//                state.setTint(readColor);
//            } else if (data.isReceived()) {
//                state.setResource(R.drawable.msg_check_2);
//                state.setTint(receivedColor);
//            } else {
//                state.setResource(R.drawable.msg_check_1);
//                state.setTint(sentColor);
//            }
//            state.setVisibility(View.VISIBLE);
//        }
    }

    public void unbind() {
        this.bindedItem = null;
        this.dialogView.unbind();
    }
}