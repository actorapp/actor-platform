package im.actor.sdk.controllers.conversation.messages;

import android.view.View;
import android.widget.TextView;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.core.entity.Message;
import im.actor.core.entity.content.ServiceContent;
import im.actor.sdk.controllers.conversation.MessagesAdapter;
import im.actor.sdk.controllers.conversation.messages.preprocessor.PreprocessedData;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class ServiceHolder extends MessageHolder {

    private TextView messageText;

    public ServiceHolder(MessagesAdapter fragment, View itemView) {
        super(fragment, itemView, true);

        messageText = (TextView) itemView.findViewById(R.id.serviceMessage);
        messageText.setTextColor(ActorSDK.sharedActor().style.getConvDatetextColor());
        onConfigureViewHolder();
    }

    @Override
    protected void bindData(Message message, long readDate, long receiveDate, boolean isUpdated, PreprocessedData preprocessedData) {
        messageText.setText(messenger().getFormatter().formatFullServiceMessage(message.getSenderId(),
                (ServiceContent) message.getContent()));
    }
}
