package im.actor.sdk.controllers.conversation.messages;

import android.support.annotation.LayoutRes;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import im.actor.core.entity.Peer;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.entity.content.JsonContent;
import im.actor.runtime.json.JSONException;
import im.actor.runtime.json.JSONObject;
import im.actor.sdk.controllers.conversation.messages.content.AbsMessageViewHolder;
import im.actor.sdk.controllers.conversation.view.BubbleContainer;
import im.actor.sdk.util.ViewUtils;

public class JsonXmlBubbleLayouter extends JsonBubbleLayouter {

    private int id;

    public JsonXmlBubbleLayouter(String dataType, @LayoutRes int id, @NotNull ViewHolderCreator creator) {
        super(dataType, creator);
        this.id = id;
    }

    @Override
    public AbsMessageViewHolder onCreateViewHolder(MessagesAdapter adapter, ViewGroup root, Peer peer) {
        return creator.onCreateViewHolder(adapter, (ViewGroup) ViewUtils.inflate(id, root), peer);
    }

}
