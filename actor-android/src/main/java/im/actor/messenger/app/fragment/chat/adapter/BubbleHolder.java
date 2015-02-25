package im.actor.messenger.app.fragment.chat.adapter;

import android.content.Context;
import android.view.View;

import com.droidkit.engine.uilist.UiList;

import im.actor.messenger.app.fragment.chat.BubbleContainer;
import im.actor.messenger.app.fragment.chat.MessagesFragment;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.view.ViewHolder;
import im.actor.messenger.util.TextUtils;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;

import static im.actor.messenger.core.Core.myUid;

/**
 * Created by ex3ndr on 10.09.14.
 */
public abstract class BubbleHolder extends ViewHolder<Message> {

    protected final Peer peer;
    protected final UiList<Message> uiList;
    protected final MessagesFragment fragment;

    protected Message currentMessage;

    private BubbleContainer container;
    private boolean isFullSize;

    protected BubbleHolder(Peer peer, MessagesFragment fragment, UiList<Message> uiList) {
        this.peer = peer;
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
    public final void bind(Message message, int pos, Context context) {
        boolean isUpdated = currentMessage == null || currentMessage.getRid() != message.getRid();
        currentMessage = message;
        container.setBubbleSelected(fragment.isSelected(currentMessage.getRid()));

        boolean useDiv;
        if (pos == 0) {
            useDiv = true;
        } else {
            Message prevMessage = uiList.getItem(uiList.getSize() - pos);
            useDiv = !TextUtils.areSameDays(prevMessage.getDate(), message.getDate());
        }

        if (useDiv) {
            container.showDate(message.getDate());
        } else {
            container.hideDate();
        }

        if (!isFullSize) {
            if (message.getSenderId() == myUid()) {
                container.makeOutboundBubble();
            } else {
                container.makeInboundBubble(peer.getPeerType() == PeerType.GROUP, message.getSenderId());
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

    public void update(Message message, int pos, boolean isUpdated, Context context) {

    }

    public void onClicked() {

    }

    @Override
    public void unbind() {
        // currentRid = 0;
    }
}
