package im.actor.messenger.app.fragment.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.droidkit.engine.uilist.UiList;

import im.actor.messenger.R;
import im.actor.messenger.app.fragment.chat.BubbleContainer;
import im.actor.messenger.app.fragment.chat.MessagesFragment;
import im.actor.messenger.app.view.MessageTextFormatter;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.content.*;

/**
 * Created by ex3ndr on 25.09.14.
 */
public class ServiceHolder extends BubbleHolder {

    protected ServiceHolder(Peer peer, MessagesFragment fragment, UiList<Message> uiList) {
        super(peer, fragment, uiList);
    }

    private TextView messageText;


    @Override
    public View init(Message data, ViewGroup viewGroup, Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        BubbleContainer v = (BubbleContainer) inflater.inflate(R.layout.adapter_dialog_service, viewGroup, false);
        messageText = (TextView) v.findViewById(R.id.serviceMessage);
        initBubbleHolder(v, true);
        return v;
    }

    @Override
    public void update(Message message, int pos, boolean isUpdated, Context context) {
        super.update(message, pos, isUpdated, context);
        if (message.getContent() instanceof ServiceUserRegistered) {
            messageText.setText(MessageTextFormatter.joinedActorFull(peer.getPeerId()));
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
            messageText.setText("???");
        }
    }
}
