package im.actor;

import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.TextView;

import im.actor.core.entity.Message;
import im.actor.sdk.R;
import im.actor.sdk.controllers.conversation.messages.BaseCustomHolder;
import im.actor.sdk.controllers.conversation.messages.MessagesAdapter;
import im.actor.sdk.controllers.conversation.messages.PreprocessedData;

public class CustomTwoHolder extends BaseCustomHolder {

    TextView text;

    public CustomTwoHolder(MessagesAdapter adapter, ViewGroup viewGroup, int id, boolean isFullSize) {
        super(adapter, viewGroup, id, isFullSize);
        text = (TextView) itemView.findViewById(R.id.tv_text);
        text.setTextColor(Color.RED);

    }

    @Override
    protected void bindData(Message message, boolean isUpdated, PreprocessedData preprocessedData) {
        text.setText(((Application.CustomTwo) message.getContent()).getText());
    }
}
