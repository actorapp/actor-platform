package im.actor.messenger.app.fragment.dialogs;

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

import im.actor.core.entity.Dialog;
import im.actor.core.entity.PeerType;
import im.actor.messenger.R;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.OnItemClickedListener;
import im.actor.messenger.app.view.TintDrawable;
import im.actor.messenger.app.view.TintImageView;
import im.actor.messenger.app.view.emoji.SmileProcessor;
import im.actor.messenger.app.view.keyboard.emoji.smiles.SmilesListener;
import im.actor.runtime.android.view.BindedViewHolder;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.Value;

import static im.actor.messenger.app.core.Core.messenger;
import static im.actor.messenger.app.core.Core.myUid;
import static im.actor.messenger.app.view.emoji.SmileProcessor.emoji;

public class DialogHolder extends BindedViewHolder {

    private AvatarView avatar;
    private TextView title;
    private TextView text;
    private TextView time;

    private TintImageView state;
    private TextView counter;

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

    private final Context context;

    public DialogHolder(Context context, FrameLayout fl, final OnItemClickedListener<Dialog> onClickListener) {
        super(fl);

        this.context = context;

        final int paddingH = Screen.dp(11);
        final int paddingV = Screen.dp(9);

        pendingColor = context.getResources().getColor(R.color.chats_state_pending);
        sentColor = context.getResources().getColor(R.color.chats_state_sent);
        receivedColor = context.getResources().getColor(R.color.chats_state_delivered);
        readColor = context.getResources().getColor(R.color.chats_state_read);
        errorColor = context.getResources().getColor(R.color.chats_state_error);

        fl.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(73)));
        fl.setBackgroundResource(R.drawable.selector_fill);

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
        title.setTextColor(context.getResources().getColor(R.color.chats_title));
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
        time.setTextColor(context.getResources().getColor(R.color.chats_time));
        time.setTypeface(Fonts.regular());
        time.setTextSize(13);
        time.setPadding(Screen.dp(6), 0, 0, 0);
        time.setSingleLine();
        firstRow.addView(time);

        linearLayout.addView(firstRow);

        text = new TextView(context);
        text.setTypeface(Fonts.regular());
        text.setTextColor(context.getResources().getColor(R.color.chats_text));
        text.setTextSize(15);
        text.setPadding(0, Screen.dp(5), Screen.dp(28), 0);
        text.setSingleLine();
        text.setEllipsize(TextUtils.TruncateAt.END);
        linearLayout.addView(text);

        fl.addView(linearLayout);

        separator = new View(context);
        separator.setBackgroundColor(context.getResources().getColor(R.color.chats_divider));
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

        counter = new TextView(context);
        counter.setTextColor(context.getResources().getColor(R.color.chats_counter));
        counter.setBackgroundColor(context.getResources().getColor(R.color.chats_counter_bg));
        counter.setPadding(Screen.dp(4), 0, Screen.dp(4), 0);
        counter.setTextSize(10);
        counter.setTypeface(Fonts.regular());
        counter.setGravity(Gravity.CENTER);
        counter.setIncludeFontPadding(false);
        counter.setMinWidth(Screen.dp(14));
        {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Screen.dp(14), Gravity.BOTTOM | Gravity.RIGHT);
            params.bottomMargin = Screen.dp(12);
            params.rightMargin = Screen.dp(10);
            counter.setLayoutParams(params);
            fl.addView(counter);
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
            left = new TintDrawable(R.drawable.dialogs_group, R.color.chats_title, context);
        }
        title.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null);

        if (data.getDate() > 0) {
            time.setVisibility(View.VISIBLE);
            time.setText(messenger().getFormatter().formatShortDate(data.getDate()));
        } else {
            time.setVisibility(View.GONE);
        }

//        Bypass bypass = new Bypass(context);

//        bindedText = bypass.markdownToSpannable(messenger().getFormatter().formatDialogText(data), true);
        bindedText = messenger().getFormatter().formatDialogText(data);

        if (SmileProcessor.containsEmoji(bindedText)) {
            if (emoji().isLoaded()) {
                bindedText = emoji().processEmojiCompatMutable(bindedText, SmileProcessor.CONFIGURATION_BUBBLES);
                ;
            } else {
                emoji().registerListener(new SmilesListener() {
                    @Override
                    public void onSmilesUpdated(boolean completed) {
                        CharSequence emojiProcessed = emoji().processEmojiCompatMutable(bindedText, SmileProcessor.CONFIGURATION_DIALOGS);
                        if (text.getText().equals(bindedText)) {
                            text.setText(emojiProcessed);
                        }
                        bindedText = emojiProcessed;
                    }
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

        if (data.getPeer().getPeerType() == PeerType.PRIVATE) {
            bindedUid = data.getPeer().getPeerId();
            privateTypingListener = new ValueChangedListener<Boolean>() {
                @Override
                public void onChanged(Boolean val, Value<Boolean> Value) {
                    if (val) {
                        text.setText(messenger().getFormatter().formatTyping());
                        text.setTextColor(context.getResources().getColor(R.color.chats_typing));
                    } else {
                        text.setText(bindedText);
                        text.setTextColor(context.getResources().getColor(R.color.chats_text));
                    }
                }
            };
            messenger().getTyping(bindedUid).subscribe(privateTypingListener);
        } else if (data.getPeer().getPeerType() == PeerType.GROUP) {
            bindedGid = data.getPeer().getPeerId();
            groupTypingListener = new ValueChangedListener<int[]>() {
                @Override
                public void onChanged(int[] val, Value<int[]> Value) {
                    if (val.length != 0) {
                        if (val.length == 1) {
                            text.setText(messenger().getFormatter().formatTyping(messenger().getUsers().get(val[0]).getName().get()));
                        } else {
                            text.setText(messenger().getFormatter().formatTyping(val.length));
                        }
                        text.setTextColor(context.getResources().getColor(R.color.chats_typing));
                    } else {
                        text.setText(bindedText);
                        text.setTextColor(context.getResources().getColor(R.color.chats_text));
                    }
                }
            };
            messenger().getGroupTyping(bindedGid).subscribe(groupTypingListener);
        } else {
            text.setText(bindedText);
            text.setTextColor(context.getResources().getColor(R.color.chats_text));
        }

        if (data.getSenderId() != myUid()) {
            state.setVisibility(View.GONE);
        } else {
            switch (data.getStatus()) {
                default:
                case PENDING:
                    state.setResource(R.drawable.msg_clock);
                    state.setTint(pendingColor);
                    break;
                case SENT:
                    state.setResource(R.drawable.msg_check_1);
                    state.setTint(sentColor);
                    break;
                case RECEIVED:
                    state.setResource(R.drawable.msg_check_2);
                    state.setTint(receivedColor);
                    break;
                case READ:
                    state.setResource(R.drawable.msg_check_2);
                    state.setTint(readColor);
                    break;
                case ERROR:
                    state.setResource(R.drawable.msg_error);
                    state.setTint(errorColor);
                    break;
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