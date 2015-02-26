package im.actor.messenger.app.fragment.chat.recycler;

import android.view.View;

import im.actor.messenger.app.fragment.chat.BubbleContainer;
import im.actor.messenger.app.fragment.chat.MessagesFragment;
import im.actor.messenger.util.TextUtils;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;

import static im.actor.messenger.core.Core.myUid;

/**
 * Created by ex3ndr on 26.02.15.
 */
public abstract class MessageHolder extends BaseHolder
        implements BubbleContainer.OnAvatarClickListener, View.OnClickListener, View.OnLongClickListener {

    private MessagesFragment fragment;
    private BubbleContainer container;
    private boolean isFullSize;
    protected Message currentMessage;

    public MessageHolder(MessagesFragment fragment, View itemView, boolean isFullSize) {
        super(itemView);
        this.fragment = fragment;
        this.container = (BubbleContainer) itemView;
        this.isFullSize = isFullSize;

        if (isFullSize) {
            container.makeFullSizeBubble();
        } else {
            container.setOnClickListener((View.OnClickListener) this);
            container.setOnClickListener((BubbleContainer.OnAvatarClickListener) this);
            container.setOnLongClickListener(this);
        }
    }

    public MessagesFragment getFragment() {
        return fragment;
    }

    public Peer getPeer() {
        return fragment.getPeer();
    }

    public final void bindData(Message message, Message prev, Message next) {
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
        if (message.getRid() == fragment.getFirstUnread()) {
            container.showUnread();
        } else {
            container.hideUnread();
        }

        // Bubble layout
        if (!isFullSize) {
            if (message.getSenderId() == myUid()) {
                container.makeOutboundBubble();
            } else {
                container.makeInboundBubble(getPeer().getPeerType() == PeerType.GROUP, message.getSenderId());
            }
        }

        // Updating selection state
        container.setBubbleSelected(fragment.isSelected(currentMessage.getRid()));

        // Bind content
        bindData(message, isUpdated);

        // Notify about message view
        fragment.onItemViewed(message);


    }

    protected abstract void bindData(Message message, boolean isUpdated);

    @Override
    public void onAvatarClick(int uid) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    public void unbind() {
        currentMessage = null;
    }
}
