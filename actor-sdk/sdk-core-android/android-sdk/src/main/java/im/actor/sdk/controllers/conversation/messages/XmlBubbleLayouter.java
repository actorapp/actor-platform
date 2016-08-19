package im.actor.sdk.controllers.conversation.messages;

import android.support.annotation.LayoutRes;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import im.actor.core.entity.Peer;
import im.actor.sdk.controllers.conversation.messages.content.AbsMessageViewHolder;
import im.actor.sdk.util.ViewUtils;

public class XmlBubbleLayouter extends LambdaBubbleLayouter {

    private int id;

    public XmlBubbleLayouter(@NotNull Matcher matcher, @LayoutRes int id, @NotNull ViewHolderCreator creator) {
        super(matcher, creator);
        this.id = id;
    }

    @Override
    public AbsMessageViewHolder onCreateViewHolder(MessagesAdapter adapter, ViewGroup root, Peer peer) {
        return creator.onCreateViewHolder(adapter, (ViewGroup) ViewUtils.inflate(id, root), peer);
    }

}
