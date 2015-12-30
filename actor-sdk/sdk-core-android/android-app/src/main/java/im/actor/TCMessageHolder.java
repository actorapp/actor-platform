package im.actor;

import android.view.ViewGroup;
import android.widget.TextView;
import im.actor.core.entity.Message;
import im.actor.sdk.R;
import im.actor.sdk.controllers.conversation.messages.BaseCustomHolder;
import im.actor.sdk.controllers.conversation.messages.MessagesAdapter;
import im.actor.sdk.controllers.conversation.messages.PreprocessedData;

public class TCMessageHolder extends BaseCustomHolder {

    TextView text;

    public TCMessageHolder(MessagesAdapter adapter, ViewGroup viewGroup, int id, boolean isFullSize) {
        super(adapter, viewGroup, id, isFullSize);
        text = (TextView) itemView.findViewById(R.id.tv_text);

    }

    @Override
    protected void bindData(Message message, boolean isUpdated, PreprocessedData preprocessedData) {
        text.setText(((Application.TCBotMesaage) message.getContent()).getRawJson());
    }
}
