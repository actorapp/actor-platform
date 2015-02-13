package im.actor.messenger.app.fragment.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.droidkit.engine.uilist.UiList;

import im.actor.messenger.R;
import im.actor.messenger.app.fragment.chat.BubbleContainer;
import im.actor.messenger.app.fragment.chat.MessagesFragment;
import im.actor.messenger.app.view.MessageTextFormatter;
import im.actor.messenger.model.MessageModel;
import im.actor.messenger.model.UserModel;
import im.actor.messenger.storage.scheme.messages.types.GroupAdd;
import im.actor.messenger.storage.scheme.messages.types.GroupAvatar;
import im.actor.messenger.storage.scheme.messages.types.GroupCreated;
import im.actor.messenger.storage.scheme.messages.types.GroupKick;
import im.actor.messenger.storage.scheme.messages.types.GroupLeave;
import im.actor.messenger.storage.scheme.messages.types.GroupTitle;
import im.actor.messenger.storage.scheme.messages.types.UserAddedDeviceMessage;
import im.actor.messenger.storage.scheme.messages.types.UserRegisteredMessage;

import static im.actor.messenger.storage.KeyValueEngines.groups;
import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 25.09.14.
 */
public class ServiceHolder extends BubbleHolder {
    private int chatType;
    private int chatId;

    protected ServiceHolder(int chatType, int chatId, MessagesFragment fragment, UiList<MessageModel> uiList) {
        super(fragment, uiList);
        this.chatType = chatType;
        this.chatId = chatId;
    }

    private TextView messageText;


    @Override
    public View init(MessageModel data, ViewGroup viewGroup, Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        BubbleContainer v = (BubbleContainer) inflater.inflate(R.layout.adapter_dialog_service, viewGroup, false);
        messageText = (TextView) v.findViewById(R.id.serviceMessage);
        initBubbleHolder(v, true);
        return v;
    }

    @Override
    public void update(MessageModel message, int pos, boolean isUpdated, Context context) {
        super.update(message, pos, isUpdated, context);
        if (message.getContent() instanceof UserRegisteredMessage) {
            messageText.setText(MessageTextFormatter.joinedActorFull(chatId));
        } else if (message.getContent() instanceof UserAddedDeviceMessage) {
            messageText.setText(MessageTextFormatter.newDeviceFull(chatId));
        } else if (message.getContent() instanceof GroupCreated) {
            messageText.setText(MessageTextFormatter.groupCreatedFull(message.getRaw().getSenderId(),
                    ((GroupCreated) message.getContent()).getTitle()));
        } else if (message.getContent() instanceof GroupLeave) {
            messageText.setText(MessageTextFormatter.groupLeave(message.getRaw().getSenderId()));
        } else if (message.getContent() instanceof GroupAdd) {
            messageText.setText(MessageTextFormatter.groupAdd(message.getRaw().getSenderId(),
                    ((GroupAdd) message.getContent()).getAddedUid()));
        } else if (message.getContent() instanceof GroupKick) {
            messageText.setText(MessageTextFormatter.groupKicked(message.getRaw().getSenderId(),
                    ((GroupKick) message.getContent()).getKickedUid()));
        } else if (message.getContent() instanceof GroupTitle) {
            messageText.setText(MessageTextFormatter.groupChangeTitleFull(message.getRaw().getSenderId(),
                    ((GroupTitle) message.getContent()).getNewTitle()));
        } else if (message.getContent() instanceof GroupAvatar) {
            GroupAvatar avatar = (GroupAvatar) message.getContent();
            if (avatar.getNewAvatar() != null) {
                messageText.setText(MessageTextFormatter.groupChangeAvatar(message.getRaw().getSenderId()));
            } else {
                messageText.setText(MessageTextFormatter.groupRemoveAvatar(message.getRaw().getSenderId()));
            }
        } else {
            messageText.setText("???");
        }
    }
}
