package im.actor.sdk.controllers.dialogs.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.drawable.RoundedCornersDrawable;

import im.actor.core.entity.ContentType;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.PeerType;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.R;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.avatar.AvatarView;
import im.actor.sdk.util.Fonts;
import im.actor.sdk.view.adapters.OnItemClickedListener;
import im.actor.sdk.view.TintDrawable;
import im.actor.sdk.view.TintImageView;
import im.actor.sdk.view.emoji.SmileProcessor;
import im.actor.sdk.view.emoji.keyboard.emoji.smiles.SmilesListener;
import im.actor.runtime.android.view.BindedViewHolder;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.Value;

import static im.actor.sdk.view.emoji.SmileProcessor.emoji;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.myUid;

public class DialogHolder extends BindedViewHolder {

    protected ActorStyle style = ActorSDK.sharedActor().style;

    private final Context context;
    private AvatarView avatar;
    private TextView title;
    private TextView text;
    private TextView time;
    private TintImageView state;
    protected TextView counter;
    private View separator;
    private CharSequence bindedText;
    private int bindedUid;
    private int bindedGid;
    private ValueChangedListener<Boolean> privateTypingListener;
    private ValueChangedListener<int[]> groupTypingListener;
    private Dialog bindedItem;
    private int pendingColor;
    private int sentColor;
    private int receivedColor;
    private int readColor;
    private int errorColor;
    private long binded;

    public DialogHolder(Context context, FrameLayout fl, final OnItemClickedListener<Dialog> onClickListener) {
        super(fl);

        this.context = context;

        final int paddingH = Screen.dp(8);
        final int paddingV = Screen.dp(8);

        pendingColor = style.getDialogsStatePendingColor();
        sentColor = style.getDialogsStateSentColor();
        receivedColor = style.getDialogsStateDeliveredColor();
        readColor = style.getDialogsStateReadColor();
        errorColor = style.getDialogsStateErrorColor();

        fl.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(73)));
        fl.setBackgroundColor(style.getMainBackgroundColor());
        FrameLayout background = new FrameLayout(context);
        background.setBackgroundResource(R.drawable.selector_fill);
        fl.addView(background, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        avatar = new AvatarView(context);
        avatar.init(Screen.dp(56), 22);
        {
            FrameLayout.LayoutParams avatarLayoutParams = new FrameLayout.LayoutParams(Screen.dp(56), Screen.dp(56));
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
            layoutParams.rightMargin = Screen.dp(16);
            layoutParams.leftMargin = Screen.dp(76);
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
        title.setTextSize(16);
        title.setPadding(0, Screen.dp(4), 0, 0);
        title.setSingleLine();
        title.setCompoundDrawablePadding(Screen.dp(4));
        title.setEllipsize(TextUtils.TruncateAt.END);
        {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, Screen.dp(19 + 4));
            params.weight = 1;
            title.setLayoutParams(params);
        }
        firstRow.addView(title);

        time = new TextView(context);
        time.setTextColor(ActorSDK.sharedActor().style.getDialogsTimeColor());
        time.setTypeface(Fonts.regular());
        time.setTextSize(14);
        time.setPadding(Screen.dp(6), 0, 0, 0);
        time.setSingleLine();
        firstRow.addView(time);

        linearLayout.addView(firstRow);

        text = new TextView(context);
        text.setTypeface(Fonts.regular());
        text.setTextColor(style.getDialogsTextColor());
        text.setTextSize(16);
        text.setPadding(0, Screen.dp(6), Screen.dp(28), 0);
        text.setSingleLine();
        text.setEllipsize(TextUtils.TruncateAt.END);
        linearLayout.addView(text);

        fl.addView(linearLayout);

        separator = new View(context);
        separator.setBackgroundColor(style.getDialogsDividerColor());
        FrameLayout.LayoutParams divLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                context.getResources().getDimensionPixelSize(R.dimen.div_size));
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

        onCreateCounter(context, style);
        {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.RIGHT);
            params.bottomMargin = Screen.dp(15);
            params.rightMargin = Screen.dp(16);
            counter.setLayoutParams(params);
            fl.addView(counter);
        }

        fl.setOnClickListener(v -> {
            if (bindedItem != null) {
                onClickListener.onClicked(bindedItem);
            }
        });
        fl.setOnLongClickListener(v -> {
            if (bindedItem != null) {
                return onClickListener.onLongClicked(bindedItem);
            }
            return false;
        });
    }

    protected void onCreateCounter(Context context, ActorStyle style) {
        counter = new TextView(context);
        counter.setTextColor(style.getDialogsCounterTextColor());
        RoundedCornersDrawable bg = new RoundedCornersDrawable(
                new ColorDrawable(style.getDialogsCounterBackgroundColor()));
        bg.setRadius(Screen.dp(2));
        bg.setOverlayColor(style.getMainBackgroundColor());
        counter.setBackgroundDrawable(bg);
        counter.setPadding(Screen.dp(4), 0, Screen.dp(4), 0);
        counter.setTextSize(10);
        counter.setTypeface(Fonts.regular());
        counter.setGravity(Gravity.CENTER);
        counter.setIncludeFontPadding(false);
        counter.setMinWidth(Screen.dp(14));
        counter.setMinHeight(Screen.dp(14));
    }

    public void bind(Dialog data, boolean isLast) {
        this.binded = data.getPeer().getUnuqueId();
        this.bindedItem = data;

        avatar.bind(data);

        if (data.getUnreadCount() > 0) {
            counter.setText(Integer.toString(data.getUnreadCount()));
            counter.setVisibility(View.VISIBLE);
        } else {
            counter.setVisibility(View.GONE);
        }

        title.setText(data.getDialogTitle());

        Drawable left = null;
        if (data.getPeer().getPeerType() == PeerType.GROUP) {
            left = new TintDrawable(context.getResources().getDrawable(R.drawable.dialogs_group), ActorSDK.sharedActor().style.getDialogsTitleColor());
        }
        title.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null);

        if (data.getDate() > 0) {
            time.setVisibility(View.VISIBLE);
            time.setText(messenger().getFormatter().formatShortDate(data.getDate()));
        } else {
            time.setVisibility(View.GONE);
        }

        bindedText = messenger().getFormatter().formatDialogText(data);
        if (SmileProcessor.containsEmoji(bindedText)) {
            if (emoji().isLoaded()) {
                bindedText = emoji().processEmojiCompatMutable(bindedText, SmileProcessor.CONFIGURATION_BUBBLES);
            } else {
                emoji().registerListener(completed -> {
                    CharSequence emojiProcessed = emoji().processEmojiCompatMutable(bindedText, SmileProcessor.CONFIGURATION_DIALOGS);
                    if (text.getText().equals(bindedText)) {
                        text.setText(emojiProcessed);
                    }
                    bindedText = emojiProcessed;
                });
            }
        }

        if (privateTypingListener != null) {
            messenger().getTyping(bindedUid).unsubscribe(privateTypingListener);
            privateTypingListener = null;
        }

        if (groupTypingListener != null) {
            messenger().getGroupTyping(bindedGid).unsubscribe(groupTypingListener);
            groupTypingListener = null;
        }

        final int textColor;
        if (data.getMessageType() != ContentType.TEXT) {
            textColor = style.getDialogsActiveTextColor();
        } else {
            textColor = style.getDialogsTextColor();
        }
        if (data.getPeer().getPeerType() == PeerType.PRIVATE) {
            bindedUid = data.getPeer().getPeerId();
            privateTypingListener = (val, Value) -> {
                if (val) {
                    text.setText(messenger().getFormatter().formatTyping());
                    text.setTextColor(ActorSDK.sharedActor().style.getDialogsTypingColor());
                } else {
                    text.setText(bindedText);
                    text.setTextColor(textColor);
                }
            };
            messenger().getTyping(bindedUid).subscribe(privateTypingListener);
        } else if (data.getPeer().getPeerType() == PeerType.GROUP) {
            bindedGid = data.getPeer().getPeerId();
            groupTypingListener = (val, Value) -> {
                if (val.length != 0) {
                    if (val.length == 1) {
                        text.setText(messenger().getFormatter().formatTyping(messenger().getUsers().get(val[0]).getName().get()));
                    } else {
                        text.setText(messenger().getFormatter().formatTyping(val.length));
                    }
                    text.setTextColor(ActorSDK.sharedActor().style.getDialogsTypingColor());
                } else {
                    text.setText(bindedText);
                    text.setTextColor(textColor);
                }
            };
            messenger().getGroupTyping(bindedGid).subscribe(groupTypingListener);
        } else {
            text.setText(bindedText);
            text.setTextColor(textColor);
        }

        if (data.getSenderId() != myUid() || data.getUnreadCount() > 0) {
            state.setVisibility(View.GONE);
        } else {
            if (data.isRead()) {
                state.setResource(R.drawable.msg_check_2);
                state.setTint(readColor);
            } else if (data.isReceived()) {
                state.setResource(R.drawable.msg_check_2);
                state.setTint(receivedColor);
            } else {
                state.setResource(R.drawable.msg_check_1);
                state.setTint(sentColor);
            }
            state.setVisibility(View.VISIBLE);
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

        if (privateTypingListener != null) {
            messenger().getTyping(bindedUid).unsubscribe(privateTypingListener);
            privateTypingListener = null;
        }

        if (groupTypingListener != null) {
            messenger().getGroupTyping(bindedGid).unsubscribe(groupTypingListener);
            groupTypingListener = null;
        }
    }
}