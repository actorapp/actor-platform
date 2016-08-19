package im.actor.sdk.controllers.conversation.messages;

import android.view.ViewGroup;

import im.actor.core.entity.Peer;
import im.actor.core.entity.content.AbsContent;
import im.actor.sdk.controllers.conversation.messages.content.AbsMessageViewHolder;

public interface BubbleLayouter {

    boolean isMatch(AbsContent content);

    AbsMessageViewHolder onCreateViewHolder(MessagesAdapter adapter, ViewGroup root, Peer peer);
}