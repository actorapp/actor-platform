package im.actor.holders;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.entity.content.TextContent;
import im.actor.develop.R;
import im.actor.sdk.controllers.conversation.messages.BubbleLayouter;
import im.actor.sdk.controllers.conversation.messages.MessagesAdapter;
import im.actor.sdk.controllers.conversation.messages.content.MessageHolder;
import im.actor.sdk.controllers.conversation.messages.content.AbsMessageViewHolder;
import im.actor.sdk.controllers.conversation.messages.content.preprocessor.PreprocessedData;
import im.actor.sdk.controllers.conversation.view.BubbleContainer;

public class BubbleTextHolderLayouter implements BubbleLayouter {

    @Override
    public boolean isMatch(AbsContent content) {
        return content instanceof TextContent;
    }

    @Override
    public AbsMessageViewHolder onCreateViewHolder(MessagesAdapter adapter, ViewGroup root, Peer peer) {
        TextView itemView = new TextView(root.getContext());
        itemView.setId(R.id.text);
        BubbleContainer container = new BubbleContainer(root.getContext());
        container.addView(itemView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        return new TextHolderEx(adapter, container);
    }

    private class TextHolderEx extends MessageHolder {
        TextView tv;

        public TextHolderEx(MessagesAdapter adapter, View itemView) {
            super(adapter, itemView, false);
            tv = (TextView) container.findViewById(R.id.text);
            tv.setTextColor(Color.RED);
        }

        @Override
        protected void bindData(Message message, long readDate, long receiveDate, boolean isUpdated, PreprocessedData preprocessedData) {
            TextContent content = (TextContent) message.getContent();
            tv.setText(content.getText());
        }
    }
}
