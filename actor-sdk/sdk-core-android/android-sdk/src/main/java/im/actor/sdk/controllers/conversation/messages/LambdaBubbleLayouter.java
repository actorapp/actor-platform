package im.actor.sdk.controllers.conversation.messages;

import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import im.actor.core.entity.Peer;
import im.actor.core.entity.content.AbsContent;
import im.actor.sdk.controllers.conversation.messages.content.AbsMessageViewHolder;

public class LambdaBubbleLayouter implements BubbleLayouter {
    protected Matcher matcher;
    protected ViewHolderCreator creator;

    public LambdaBubbleLayouter(@NotNull Matcher matcher, @NotNull ViewHolderCreator creator) {
        this.matcher = matcher;
        this.creator = creator;
    }

    @Override
    public boolean isMatch(AbsContent content) {
        return matcher.isMatch(content);
    }

    @Override
    public AbsMessageViewHolder onCreateViewHolder(MessagesAdapter adapter, ViewGroup root, Peer peer) {
        return creator.onCreateViewHolder(adapter, root, peer);
    }

    public interface Matcher {
        boolean isMatch(AbsContent content);
    }

    public interface ViewHolderCreator {
        AbsMessageViewHolder onCreateViewHolder(MessagesAdapter adapter, ViewGroup root, Peer peer);
    }
}
