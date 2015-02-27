package im.actor.messenger.app.fragment.chat.recycler;

import android.view.View;
import android.widget.TextView;

import im.actor.messenger.R;
import im.actor.messenger.app.fragment.chat.MessagesFragment;
import im.actor.messenger.app.view.MessageTextFormatter;
import im.actor.model.entity.Message;
import im.actor.model.entity.content.ServiceContent;
import im.actor.model.entity.content.ServiceGroupAvatarChanged;
import im.actor.model.entity.content.ServiceGroupCreated;
import im.actor.model.entity.content.ServiceGroupTitleChanged;
import im.actor.model.entity.content.ServiceGroupUserAdded;
import im.actor.model.entity.content.ServiceGroupUserKicked;
import im.actor.model.entity.content.ServiceGroupUserLeave;
import im.actor.model.entity.content.ServiceUserRegistered;

/**
 * Created by ex3ndr on 27.02.15.
 */
public class ServiceHolder extends MessageHolder {

    private TextView messageText;

    public ServiceHolder(MessagesFragment fragment, View itemView) {
        super(fragment, itemView, true);

        messageText = (TextView) itemView.findViewById(R.id.serviceMessage);
    }

    @Override
    protected void bindData(Message message, boolean isUpdated) {
        if (message.getContent() instanceof ServiceUserRegistered) {
            messageText.setText(MessageTextFormatter.joinedActorFull(getPeer().getPeerId()));
        } else if (message.getContent() instanceof ServiceGroupCreated) {
            messageText.setText(MessageTextFormatter.groupCreatedFull(message.getSenderId(),
                    ((ServiceGroupCreated) message.getContent()).getGroupTitle()));
        } else if (message.getContent() instanceof ServiceGroupUserLeave) {
            messageText.setText(MessageTextFormatter.groupLeave(message.getSenderId()));
        } else if (message.getContent() instanceof ServiceGroupUserAdded) {
            messageText.setText(MessageTextFormatter.groupAdd(message.getSenderId(),
                    ((ServiceGroupUserAdded) message.getContent()).getAddedUid()));
        } else if (message.getContent() instanceof ServiceGroupUserKicked) {
            messageText.setText(MessageTextFormatter.groupKicked(message.getSenderId(),
                    ((ServiceGroupUserKicked) message.getContent()).getKickedUid()));
        } else if (message.getContent() instanceof ServiceGroupTitleChanged) {
            messageText.setText(MessageTextFormatter.groupChangeTitleFull(message.getSenderId(),
                    ((ServiceGroupTitleChanged) message.getContent()).getNewTitle()));
        } else if (message.getContent() instanceof ServiceGroupAvatarChanged) {
            ServiceGroupAvatarChanged avatar = (ServiceGroupAvatarChanged) message.getContent();
            if (avatar.getNewAvatar() != null) {
                messageText.setText(MessageTextFormatter.groupChangeAvatar(message.getSenderId()));
            } else {
                messageText.setText(MessageTextFormatter.groupRemoveAvatar(message.getSenderId()));
            }
        } else {
            messageText.setText(((ServiceContent) message.getContent()).getCompatText());
        }
    }
}
