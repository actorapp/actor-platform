package im.actor.messenger.app.fragment.chat.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.droidkit.engine.uilist.UiList;

import im.actor.messenger.R;
import im.actor.messenger.app.fragment.chat.BubbleContainer;
import im.actor.messenger.app.fragment.chat.MessagesFragment;
import im.actor.messenger.app.intents.Intents;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.ViewHolder;
import im.actor.messenger.model.DialogType;
import im.actor.messenger.model.MessageModel;
import im.actor.messenger.util.TextUtils;

/**
 * Created by ex3ndr on 10.09.14.
 */
public abstract class BubbleHolder extends ViewHolder<MessageModel> {

    private UiList<MessageModel> uiList;
    private MessagesFragment fragment;

    private BubbleContainer container;

    private MessageModel currentMessage;

    private boolean isFullSize;

    protected BubbleHolder(MessagesFragment fragment, UiList<MessageModel> uiList) {
        this.uiList = uiList;
        this.fragment = fragment;
    }

    public MessagesFragment getFragment() {
        return fragment;
    }

    protected void initBubbleHolder(final BubbleContainer container, boolean isFullSize) {
        this.container = container;
        this.isFullSize = isFullSize;
        if (isFullSize) {
            container.makeFullSizeBubble();
        }

        if (!isFullSize) {
            container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    boolean res = fragment.onLongClick(currentMessage);
                    container.setBubbleSelected(fragment.isSelected(currentMessage.getRid()));
                    return res;
                }
            });
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!fragment.onClick(currentMessage)) {
                        onClicked();
                    } else {
                        container.setBubbleSelected(fragment.isSelected(currentMessage.getRid()));
                    }
                }
            });
            container.setOnClickListener(new BubbleContainer.OnAvatarClickListener() {
                @Override
                public void onAvatarClick(int uid) {
                    fragment.startActivity(Intents.openProfile(uid, fragment.getActivity()));
                }
            });
        }
    }

    @Override
    public final void bind(MessageModel message, int pos, Context context) {
        boolean isUpdated = currentMessage == null || currentMessage.getRid() != message.getRid();
        currentMessage = message;
        container.setBubbleSelected(fragment.isSelected(currentMessage.getRid()));

        boolean useDiv;
        if (pos == 0) {
            useDiv = true;
        } else {
            MessageModel prevMessage = uiList.getItem(uiList.getSize() - pos);
            useDiv = !TextUtils.areSameDays(prevMessage.getRaw().getTime(), message.getRaw().getTime());
        }

        if (useDiv) {
            container.showDate(message.getRaw().getTime());
        } else {
            container.hideDate();
        }

        if (!isFullSize) {
            if (message.isOut()) {
                container.makeOutboundBubble();
            } else {
                container.makeInboundBubble(fragment.getChatType() == DialogType.TYPE_GROUP,
                        message.getRaw().getSenderId());
            }
        }

        if (message.getRid() == fragment.getFirstUnread()) {
            container.showUnread();
        } else {
            container.hideUnread();
        }

        update(message, pos, isUpdated, context);

        fragment.onItemViewed(message);
    }

    public void update(MessageModel message, int pos, boolean isUpdated, Context context) {

    }

    public void onClicked() {

    }

    @Override
    public void unbind() {
        // currentRid = 0;
    }
}
