package im.actor.sdk.controllers.conversation.messages.content;

import android.view.View;
import android.widget.TextView;

import im.actor.core.entity.GroupType;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.core.entity.Message;
import im.actor.core.entity.content.ServiceContent;
import im.actor.sdk.controllers.conversation.messages.MessagesAdapter;
import im.actor.sdk.controllers.conversation.messages.content.preprocessor.PreprocessedData;

import static im.actor.sdk.util.ActorSDKMessenger.groups;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class ServiceHolder extends MessageHolder {

    private TextView messageText;
    private boolean isChannel;

    public ServiceHolder(MessagesAdapter adapter, View itemView, Peer peer) {
        super(adapter, itemView, true);

        isChannel = peer.getPeerType() == PeerType.GROUP && groups().get(peer.getPeerId()).getGroupType() == GroupType.CHANNEL;

        messageText = (TextView) itemView.findViewById(R.id.serviceMessage);
        messageText.setTextColor(ActorSDK.sharedActor().style.getConvDatetextColor());
        onConfigureViewHolder();
    }

    @Override
    protected void bindData(Message message, long readDate, long receiveDate, boolean isUpdated, PreprocessedData preprocessedData) {
        messageText.setText(messenger().getFormatter().formatFullServiceMessage(message.getSenderId(),
                (ServiceContent) message.getContent(), isChannel));
    }
}
