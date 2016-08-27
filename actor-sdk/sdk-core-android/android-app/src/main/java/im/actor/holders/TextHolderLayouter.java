package im.actor.holders;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.entity.content.TextContent;
import im.actor.sdk.controllers.conversation.messages.BubbleLayouter;
import im.actor.sdk.controllers.conversation.messages.MessagesAdapter;
import im.actor.sdk.controllers.conversation.messages.content.AbsMessageViewHolder;
import im.actor.sdk.controllers.conversation.messages.content.preprocessor.PreprocessedData;

public class TextHolderLayouter implements BubbleLayouter {
    @Override
    public boolean isMatch(AbsContent content) {
        return content instanceof TextContent;
    }

    @Override
    public AbsMessageViewHolder onCreateViewHolder(MessagesAdapter adapter, ViewGroup root, Peer peer) {
        return new TextHolderEx(new TextView(root.getContext()));
    }

    private class TextHolderEx extends AbsMessageViewHolder {
        TextView tv;

        public TextHolderEx(View itemView) {
            super(itemView);
            tv = (TextView) itemView;
            tv.setTextColor(Color.RED);
        }

        @Override
        public void bindData(Message message, Message prev, Message next, long readDate, long receiveDate, PreprocessedData preprocessedData) {
            TextContent content = (TextContent) message.getContent();
            tv.setText(content.getText());
        }

        @Override
        public void unbind() {

        }
    }
}
