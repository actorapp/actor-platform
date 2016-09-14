package im.actor.sdk.controllers.conversation.messages;

import android.view.ViewGroup;

import java.util.ArrayList;

import im.actor.core.entity.Peer;
import im.actor.core.entity.content.AbsContent;
import im.actor.sdk.controllers.conversation.messages.content.AbsMessageViewHolder;
import im.actor.sdk.controllers.conversation.messages.content.UnsupportedHolder;
import im.actor.sdk.util.ViewUtils;
import im.actor.sdk.R;

public class ViewHolderMatcher {
    ArrayList<BubbleLayouter> layouters = new ArrayList<>();

    public ViewHolderMatcher add(BubbleLayouter layouter) {
        layouters.add(layouter);
        return this;
    }

    public ViewHolderMatcher addToTop(BubbleLayouter layouter) {
        layouters.add(0, layouter);
        return this;
    }


    public int getMatchId(AbsContent content) {
        for (int i = 0; i < layouters.size(); i++) {
            if (layouters.get(i).isMatch(content)) {
                return i;
            }
        }
        return -1;
    }

    public AbsMessageViewHolder onCreateViewHolder(int id, MessagesAdapter adapter, ViewGroup root, Peer peer) {
        if (id == -1) {
            return new UnsupportedHolder(adapter, ViewUtils.inflate(R.layout.adapter_dialog_text, root), peer);
        }
        BubbleLayouter baseViewHolderMatch = layouters.get(id);
        return baseViewHolderMatch.onCreateViewHolder(adapter, root, peer);
    }

    public ArrayList<BubbleLayouter> getLayouters() {
        return layouters;
    }
}