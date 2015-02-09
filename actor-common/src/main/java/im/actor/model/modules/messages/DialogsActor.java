package im.actor.model.modules.messages;

import com.droidkit.actors.Actor;
import im.actor.model.Messenger;
import im.actor.model.entity.*;
import im.actor.model.mvvm.ListEngine;

import static im.actor.model.util.JavaUtil.equalsE;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class DialogsActor extends Actor {

    private Messenger messenger;
    private ListEngine<Dialog> dialogs;

    public DialogsActor(Messenger messenger) {
        this.messenger = messenger;
        this.dialogs = messenger.getDialogs();
    }

    public Messenger getMessenger() {
        return messenger;
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof InMessage) {
            InMessage inMessage = (InMessage) message;
            onMessage(inMessage.getPeer(), inMessage.getMessage());
        } else if (message instanceof UserChanged) {
            UserChanged userChanged = (UserChanged) message;
            onUserChanged(userChanged.getUser());
        } else if (message instanceof ChatClear) {
            onChatClear(((ChatClear) message).getPeer());
        } else if (message instanceof ChatDelete) {
            onChatDeleted(((ChatDelete) message).getPeer());
        }
    }

    private void onMessage(Peer peer, Message message) {
        String name;
        Avatar avatar;
        switch (peer.getPeerType()) {
            case PRIVATE:
                User u = messenger.getUsers().getValue(peer.getPeerId());
                name = u.getName();
                avatar = u.getAvatar();
                break;
            default:
                return;
        }

        Dialog dialog = dialogs.getValue(peer.getUid());

        ContentDescription contentDescription = new ContentDescription(message.getContent());

        DialogBuilder builder = new DialogBuilder()
                .setRid(message.getRid())
                .setTime(message.getTime())
                .setMessageType(contentDescription.getContentType())
                .setText(contentDescription.getText())
                .setRelatedUid(contentDescription.getRelatedUid())
                .setStatus(message.getMessageState())
                .setSenderId(message.getSenderId());

        if (dialog != null) {
            // Ignore old messages
            if (dialog.getSortKey() > message.getSortKey()) {
                return;
            }

            builder.setPeer(dialog.getPeer())
                    .setDialogTitle(dialog.getDialogTitle())
                    .setDialogAvatar(dialog.getDialogAvatar())
                    .setUnreadCount(dialog.getUnreadCount());

            // Do not push up dialogs for silent messages
            if (!contentDescription.isSilent()) {
                builder.setSortKey(message.getSortKey());
            }
        } else {
            // Do not create dialogs for silent messages
            if (contentDescription.isSilent()) {
                return;
            }

            builder.setPeer(peer)
                    .setDialogTitle(name)
                    .setDialogAvatar(avatar)
                    .setUnreadCount(0)
                    .setSortKey(message.getSortKey());
        }

        dialogs.addOrUpdateItem(builder.createDialog());
    }

    private void onUserChanged(User user) {
        Dialog dialog = dialogs.getValue(user.peer().getUid());
        if (dialog != null) {
            if (dialog.getDialogTitle().equals(user.getName())
                    && equalsE(dialog.getDialogAvatar(), user.getAvatar())) {
                return;
            }
            dialogs.addOrUpdateItem(dialog.editPeerInfo(user.getName(), user.getAvatar()));
        }
    }

    private void onChatDeleted(Peer peer) {
        dialogs.removeItem(peer.getUid());
    }

    private void onChatClear(Peer peer) {
        Dialog dialog = dialogs.getValue(peer.getUid());
        if (dialog != null) {
            dialogs.addOrUpdateItem(new DialogBuilder(dialog)
                    .setMessageType(Dialog.ContentType.EMPTY)
                    .setText(null)
                    .setTime(0)
                    .setUnreadCount(0)
                    .setRid(0)
                    .createDialog());
        }
    }

    public static class ChatClear {
        private Peer peer;

        public ChatClear(Peer peer) {
            this.peer = peer;
        }

        public Peer getPeer() {
            return peer;
        }
    }

    public static class ChatDelete {
        private Peer peer;

        public ChatDelete(Peer peer) {
            this.peer = peer;
        }

        public Peer getPeer() {
            return peer;
        }
    }

    public static class InMessage {
        private Peer peer;
        private Message message;

        public InMessage(Peer peer, Message message) {
            this.peer = peer;
            this.message = message;
        }

        public Peer getPeer() {
            return peer;
        }

        public Message getMessage() {
            return message;
        }
    }

    public static class UserChanged {
        private User user;

        public UserChanged(User user) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }
    }
}
