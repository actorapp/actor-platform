package im.actor.messenger.app.fragment.chat.messages;

import android.view.View;

import im.actor.core.viewmodel.UserVM;
import im.actor.messenger.app.fragment.chat.view.BubbleContainer;
import im.actor.messenger.app.util.TextUtils;
import im.actor.runtime.android.view.BindedViewHolder;
import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;

import static im.actor.messenger.app.core.Core.groups;
import static im.actor.messenger.app.core.Core.myUid;
import static im.actor.messenger.app.core.Core.users;

public abstract class MessageHolder extends BindedViewHolder
        implements BubbleContainer.OnAvatarClickListener, BubbleContainer.OnAvatarLongClickListener, View.OnClickListener, View.OnLongClickListener {

    private MessagesAdapter adapter;
    protected BubbleContainer container;
    private boolean isFullSize;
    protected Message currentMessage;

    public MessageHolder(MessagesAdapter adapter, View itemView, boolean isFullSize) {
        super(itemView);
        this.adapter = adapter;
        this.container = (BubbleContainer) itemView;
        this.isFullSize = isFullSize;

        if (isFullSize) {
            container.makeFullSizeBubble();
        } else {
            container.setOnClickListener((View.OnClickListener) this);
            container.setOnClickListener((BubbleContainer.OnAvatarClickListener) this);
            container.setOnLongClickListener((View.OnLongClickListener) this);
            container.setOnLongClickListener((BubbleContainer.OnAvatarLongClickListener) this);
        }
    }

    public MessagesAdapter getAdapter() {
        return adapter;
    }

    public Peer getPeer() {
        return adapter.getMessagesFragment().getPeer();
    }

    public final void bindData(Message message, Message prev, Message next, PreprocessedData preprocessedData) {
        boolean isUpdated = currentMessage == null || currentMessage.getRid() != message.getRid();
        currentMessage = message;

        // Date div
        boolean useDiv;
        if (prev != null) {
            useDiv = !TextUtils.areSameDays(prev.getDate(), message.getDate());
        } else {
            useDiv = true;
        }
        if (useDiv) {
            container.showDate(message.getDate());
        } else {
            container.hideDate();
        }

        // Unread
        if (message.getRid() == adapter.getFirstUnread()) {
            container.showUnread();
        } else {
            container.hideUnread();
        }

        // Bubble layout
        if (!isFullSize) {
            if (message.getSenderId() == myUid()) {
                container.makeOutboundBubble();
            } else {
                boolean isGroupBot = getPeer().getPeerType().equals(PeerType.GROUP) && users().get(message.getSenderId()).getName().get().equals("Bot");
                container.makeInboundBubble(getPeer().getPeerType() == PeerType.GROUP, message.getSenderId(), isGroupBot ? getPeer().getPeerId() : 0);
            }
        }

        // Updating selection state
        container.setBubbleSelected(adapter.isSelected(currentMessage));

        // Bind content
        bindData(message, isUpdated, preprocessedData);
    }

    protected abstract void bindData(Message message, boolean isUpdated, PreprocessedData preprocessedData);

    @Override
    public void onAvatarClick(int uid) {
        adapter.getMessagesFragment().onAvatarClick(uid);
    }

    @Override
    public void onAvatarLongClick(int uid) {
        adapter.getMessagesFragment().onAvatarLongClick(uid);
    }

    @Override
    public final void onClick(View v) {
        if (currentMessage != null) {
            if (!adapter.getMessagesFragment().onClick(currentMessage)) {
                onClick(currentMessage);
            }
        }
    }

    public void onClick(Message currentMessage) {

    }

    @Override
    public boolean onLongClick(View v) {
        if (currentMessage != null) {
            return adapter.getMessagesFragment().onLongClick(currentMessage);
        }
        return false;
    }

    public void unbind() {
        currentMessage = null;
    }
}
