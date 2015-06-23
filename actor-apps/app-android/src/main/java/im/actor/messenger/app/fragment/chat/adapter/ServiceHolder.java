package im.actor.messenger.app.fragment.chat.adapter;

import android.view.View;
import android.widget.TextView;

import im.actor.messenger.R;
import im.actor.messenger.app.fragment.chat.MessagesAdapter;
import im.actor.model.entity.Message;
import im.actor.model.entity.content.ServiceContent;

import static im.actor.messenger.app.Core.messenger;

/**
 * Created by ex3ndr on 27.02.15.
 */
public class ServiceHolder extends MessageHolder {

    private TextView messageText;

    public ServiceHolder(MessagesAdapter fragment, View itemView) {
        super(fragment, itemView, true);

        messageText = (TextView) itemView.findViewById(R.id.serviceMessage);
    }

    @Override
    protected void bindData(Message message, boolean isUpdated) {
        messageText.setText(messenger().getFormatter().formatFullServiceMessage(message.getSenderId(),
                (ServiceContent) message.getContent()));
    }
}
