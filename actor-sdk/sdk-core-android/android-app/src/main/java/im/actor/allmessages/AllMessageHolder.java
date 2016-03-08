package im.actor.allmessages;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import im.actor.core.entity.ContentDescription;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.MessageState;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.runtime.android.view.BindedViewHolder;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.util.Fonts;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.TintDrawable;
import im.actor.sdk.view.TintImageView;
import im.actor.sdk.view.adapters.OnItemClickedListener;
import im.actor.sdk.view.avatar.AvatarView;
import im.actor.sdk.view.emoji.SmileProcessor;
import im.actor.sdk.view.emoji.keyboard.emoji.smiles.SmilesListener;

import static im.actor.sdk.util.ActorSDKMessenger.groups;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.myUid;
import static im.actor.sdk.util.ActorSDKMessenger.users;
import static im.actor.sdk.view.emoji.SmileProcessor.emoji;

public class AllMessageHolder extends BindedViewHolder {
    private final AvatarView avatar;
    private final TextView title;
    private final TextView time;
    private final View separator;
    private final TintImageView state;
    private final TextView text;
    Context context;
    private MessageEx bindedItem;
    private OnItemClickedListener<MessageEx> onClickListener;

    public AllMessageHolder(FrameLayout fl, Context context, final OnItemClickedListener<MessageEx> onClickListener) {
        super(fl);

        this.context = context;
        this.onClickListener = onClickListener;
        final int paddingH = Screen.dp(11);
        final int paddingV = Screen.dp(9);

        ActorStyle style = ActorSDK.sharedActor().style;

        fl.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(73)));
        fl.setBackgroundColor(style.getMainBackgroundColor());
        FrameLayout background = new FrameLayout(context);
        background.setBackgroundResource(im.actor.sdk.R.drawable.selector_fill);
        fl.addView(background, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        avatar = new AvatarView(context);
        avatar.init(Screen.dp(52), 24);
        {
            FrameLayout.LayoutParams avatarLayoutParams = new FrameLayout.LayoutParams(Screen.dp(52), Screen.dp(52));
            avatarLayoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
            avatarLayoutParams.leftMargin = paddingH;
            avatar.setLayoutParams(avatarLayoutParams);
        }
        fl.addView(avatar);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.TOP);
        {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.rightMargin = paddingH;
            layoutParams.leftMargin = Screen.dp(79);
            layoutParams.topMargin = paddingV;
            layoutParams.bottomMargin = paddingV;
            linearLayout.setLayoutParams(layoutParams);
        }

        LinearLayout firstRow = new LinearLayout(context);
        firstRow.setOrientation(LinearLayout.HORIZONTAL);
        {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            firstRow.setLayoutParams(params);
        }

        title = new TextView(context);
        title.setTextColor(ActorSDK.sharedActor().style.getDialogsTitleColor());
        title.setTypeface(Fonts.medium());
        title.setTextSize(17);
        title.setPadding(0, Screen.dp(1), 0, 0);
        title.setSingleLine();
        title.setCompoundDrawablePadding(Screen.dp(4));
        title.setEllipsize(TextUtils.TruncateAt.END);
        {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.weight = 1;
            title.setLayoutParams(params);
        }
        firstRow.addView(title);

        time = new TextView(context);
        time.setTextColor(ActorSDK.sharedActor().style.getDialogsTimeColor());
        time.setTypeface(Fonts.regular());
        time.setTextSize(13);
        time.setPadding(Screen.dp(6), 0, 0, 0);
        time.setSingleLine();
        firstRow.addView(time);

        linearLayout.addView(firstRow);

        text = new TextView(context);
        text.setTypeface(Fonts.regular());
        text.setTextColor(style.getDialogsTextColor());
        text.setTextSize(15);
        text.setPadding(0, Screen.dp(5), Screen.dp(28), 0);
        text.setSingleLine();
        text.setEllipsize(TextUtils.TruncateAt.END);
        linearLayout.addView(text);



        fl.addView(linearLayout);

        separator = new View(context);
        separator.setBackgroundColor(style.getDialogsDividerColor());
        FrameLayout.LayoutParams divLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                context.getResources().getDimensionPixelSize(im.actor.sdk.R.dimen.div_size));
        divLayoutParams.leftMargin = Screen.dp(76);
        divLayoutParams.gravity = Gravity.BOTTOM;
        fl.addView(separator, divLayoutParams);

        state = new TintImageView(context);
        {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(Screen.dp(28), Screen.dp(12), Gravity.BOTTOM | Gravity.RIGHT);
            params.bottomMargin = Screen.dp(16);
            params.rightMargin = Screen.dp(9);
            state.setLayoutParams(params);
            fl.addView(state);
        }


        fl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bindedItem != null) {
                    onClickListener.onClicked(bindedItem);
                }
            }
        });
        fl.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (bindedItem != null) {
                    return onClickListener.onLongClicked(bindedItem);
                }
                return false;
            }
        });
    }

    public void bind(MessageEx data, boolean isLast) {
        Peer peer= new Peer(PeerType.PRIVATE, data.getSenderId());
        this.bindedItem = data;

        avatar.bind(users().get(data.getSenderId()));


        ContentDescription contentDescription = ContentDescription.fromContent(data.getContent());

        title.setText(messenger().getFormatter().formatDialogText(new Dialog(peer, 1, "", null, 1, 1, contentDescription.getContentType(), contentDescription.getText(), MessageState.SENT, peer.getPeerId(), 1,1)));

        text.setText(data.getPeer().getPeerType()==PeerType.PRIVATE?"in private":"at "+groups().get(data.getPeer().getPeerId()).getName().get());

        if (data.getDate() > 0) {
            time.setVisibility(View.VISIBLE);
            time.setText(messenger().getFormatter().formatShortDate(data.getDate()));
        } else {
            time.setVisibility(View.GONE);
        }


        if (isLast) {
            separator.setVisibility(View.GONE);
        } else {
            separator.setVisibility(View.VISIBLE);
        }
    }

    public void unbind() {
        this.bindedItem = null;

        this.avatar.unbind();

    }
}
