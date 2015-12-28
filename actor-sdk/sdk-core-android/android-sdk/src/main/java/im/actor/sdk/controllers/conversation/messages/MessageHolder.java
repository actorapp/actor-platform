package im.actor.sdk.controllers.conversation.messages;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.UserPresence;
import im.actor.core.viewmodel.UserVM;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.controllers.conversation.view.BubbleContainer;
import im.actor.sdk.controllers.fragment.ActorBinder;
import im.actor.sdk.util.Strings;
import im.actor.runtime.android.view.BindedViewHolder;
import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;

import static im.actor.sdk.util.ActorSDKMessenger.myUid;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public abstract class MessageHolder extends BindedViewHolder
        implements BubbleContainer.OnAvatarClickListener, BubbleContainer.OnAvatarLongClickListener, View.OnClickListener, View.OnLongClickListener {

    protected MessagesAdapter adapter;
    protected BubbleContainer container;
    protected boolean isFullSize;
    protected Message currentMessage;
    protected ActorBinder.Binding onlineBinding;
    protected Spannable reactions;
    protected boolean hasMyReaction;

    public MessageHolder(MessagesAdapter adapter, final View itemView, boolean isFullSize) {
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

    public void setOnline(boolean online, boolean isBot) {
        container.setOnline(online, isBot);
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
            useDiv = !Strings.areSameDays(prev.getDate(), message.getDate());
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

        //online
        if (onlineBinding != null) {
            getAdapter().getBinder().unbind(onlineBinding);
        }
        final UserVM user = users().get(message.getSenderId());
        onlineBinding = getAdapter().getBinder().bind(new ActorBinder.OnChangedListener<Boolean>() {
            @Override
            public void onChanged(Boolean online) {
                setOnline(online, user.isBot());
            }

        }, user.getPresence());
        setOnline(user.getPresence().get().getState().equals(UserPresence.State.ONLINE), user.isBot());

        hasMyReaction = false;
        if (preprocessedData != null) {
            reactions = preprocessedData.getReactionsSpannable();
            if (reactions != null) {
                for (ReactionSpan r : reactions.getSpans(0, reactions.length(), ReactionSpan.class)) {
                    if (r.hasMyReaction()) {
                        hasMyReaction = true;
                    }
                }
            }
        }
        // Bind content
        bindData(message, isUpdated, preprocessedData);
        ActorSDK.sharedActor().getMessenger().onUserVisible(message.getSenderId());
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
            return adapter.getMessagesFragment().onLongClick(currentMessage, hasMyReaction);
        }
        return false;
    }


    public void unbind() {
        currentMessage = null;
    }

    protected void setTimeAndReactions(TextView time) {
        Spannable timeWithReactions = null;
        if (reactions != null) {
            SpannableStringBuilder builder = new SpannableStringBuilder(reactions);
            timeWithReactions = builder.append(Strings.formatTime(currentMessage.getDate()));
        }
        time.setText(timeWithReactions != null ? timeWithReactions : Strings.formatTime(currentMessage.getDate()));
        time.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
