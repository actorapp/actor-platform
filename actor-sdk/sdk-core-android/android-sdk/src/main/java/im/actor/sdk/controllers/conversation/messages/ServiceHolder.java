package im.actor.sdk.controllers.conversation.messages;

import android.view.View;
import android.widget.TextView;

import im.actor.messenger.R;
import im.actor.core.entity.Message;
import im.actor.core.entity.content.ServiceContent;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class ServiceHolder extends MessageHolder {

    private TextView messageText;

    public ServiceHolder(MessagesAdapter fragment, View itemView) {
        super(fragment, itemView, true);

        messageText = (TextView) itemView.findViewById(R.id.serviceMessage);
    }

    @Override
    protected void bindData(Message message, boolean isUpdated, PreprocessedData preprocessedData) {
        messageText.setText(messenger().getFormatter().formatFullServiceMessage(message.getSenderId(),
                (ServiceContent) message.getContent()));
    }
}
