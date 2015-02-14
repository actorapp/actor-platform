package im.actor.model.modules.messages;

import im.actor.model.droidkit.actors.Actor;
import im.actor.model.Messenger;
import im.actor.model.entity.*;
import im.actor.model.modules.entity.DialogHistory;
import im.actor.model.mvvm.ListEngine;

import java.util.ArrayList;
import java.util.List;

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

    private void onMessage(Peer peer, Message message, boolean isAfterDelete) {
        PeerDesc peerDesc = buildPeerDesc(peer);
        if (peerDesc == null) {
            return;
        }

        Dialog dialog = dialogs.getValue(peer.getUid());

        ContentDescription contentDescription = new ContentDescription(message.getContent());

        DialogBuilder builder = new DialogBuilder()
                .setRid(message.getRid())
                .setTime(message.getDate())
                .setMessageType(contentDescription.getContentType())
                .setText(contentDescription.getText())
                .setRelatedUid(contentDescription.getRelatedUid())
                .setStatus(message.getMessageState())
                .setSenderId(message.getSenderId());

        if (dialog != null) {
            // Ignore old messages
            if (!isAfterDelete && dialog.getSortDate() > message.getSortDate()) {
                return;
            }

            builder.setPeer(dialog.getPeer())
                    .setDialogTitle(dialog.getDialogTitle())
                    .setDialogAvatar(dialog.getDialogAvatar())
                    .setUnreadCount(dialog.getUnreadCount());

            // Do not push up dialogs for silent messages
            if (!contentDescription.isSilent()) {
                builder.setSortKey(message.getSortDate());
            }
        } else {
            // Do not create dialogs for silent messages
            if (contentDescription.isSilent()) {
                return;
            }

            builder.setPeer(peer)
                    .setDialogTitle(peerDesc.getTitle())
                    .setDialogAvatar(peerDesc.getAvatar())
                    .setUnreadCount(0)
                    .setSortKey(message.getSortDate());
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

    private void onMessageStatusChanged(Peer peer, long rid, MessageState state) {
        Dialog dialog = dialogs.getValue(peer.getUid());
        if (dialog != null && dialog.getRid() == rid) {
            dialogs.addOrUpdateItem(new DialogBuilder(dialog)
                    .setStatus(state)
                    .createDialog());
        }
    }

    private void onCounterChanged(Peer peer, int count) {
        Dialog dialog = dialogs.getValue(peer.getUid());
        if (dialog != null) {
            dialogs.addOrUpdateItem(new DialogBuilder(dialog)
                    .setUnreadCount(count)
                    .createDialog());
        }
    }

    private void onHistoryLoaded(List<DialogHistory> history) {
        ArrayList<Dialog> updated = new ArrayList<Dialog>();
        for (DialogHistory dialogHistory : history) {
            // Ignore already available dialogs
            if (dialogs.getValue(dialogHistory.getPeer().getUid()) != null) {
                continue;
            }

            PeerDesc peerDesc = buildPeerDesc(dialogHistory.getPeer());
            if (peerDesc == null) {
                continue;
            }

            ContentDescription description = new ContentDescription(dialogHistory.getContent());

            updated.add(new Dialog(dialogHistory.getPeer(),
                    dialogHistory.getSortDate(), peerDesc.getTitle(), peerDesc.getAvatar(),
                    dialogHistory.getUnreadCount(),
                    dialogHistory.getRid(), description.getContentType(), description.getText(), dialogHistory.getStatus(),
                    dialogHistory.getSenderId(), dialogHistory.getDate(), description.getRelatedUid()));
        }
        dialogs.addOrUpdateItems(updated);
    }

    private PeerDesc buildPeerDesc(Peer peer) {
        switch (peer.getPeerType()) {
            case PRIVATE:
                User u = messenger.getUsers().getValue(peer.getPeerId());
                return new PeerDesc(u.getName(), u.getAvatar());
            default:
                return null;
        }
    }

    private class PeerDesc {

        private String title;
        private Avatar avatar;

        private PeerDesc(String title, Avatar avatar) {
            this.title = title;
            this.avatar = avatar;
        }

        public String getTitle() {
            return title;
        }

        public Avatar getAvatar() {
            return avatar;
        }
    }

    // Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof InMessage) {
            InMessage inMessage = (InMessage) message;
            onMessage(inMessage.getPeer(), inMessage.getMessage(), false);
        } else if (message instanceof UserChanged) {
            UserChanged userChanged = (UserChanged) message;
            onUserChanged(userChanged.getUser());
        } else if (message instanceof ChatClear) {
            onChatClear(((ChatClear) message).getPeer());
        } else if (message instanceof ChatDelete) {
            onChatDeleted(((ChatDelete) message).getPeer());
        } else if (message instanceof MessageStateChanged) {
            MessageStateChanged messageStateChanged = (MessageStateChanged) message;
            onMessageStatusChanged(messageStateChanged.getPeer(), messageStateChanged.getRid(),
                    messageStateChanged.getState());
        } else if (message instanceof CounterChanged) {
            CounterChanged counterChanged = (CounterChanged) message;
            onCounterChanged(counterChanged.getPeer(), counterChanged.getCount());
        } else if (message instanceof Deleted) {
            Deleted deleted = (Deleted) message;
            onMessage(deleted.getPeer(), deleted.getMessage(), true);
        } else if (message instanceof HistoryLoaded) {
            HistoryLoaded historyLoaded = (HistoryLoaded) message;
            onHistoryLoaded(historyLoaded.getHistory());
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

    public static class MessageStateChanged {
        private Peer peer;
        private long rid;
        private MessageState state;

        public MessageStateChanged(Peer peer, long rid, MessageState state) {
            this.peer = peer;
            this.rid = rid;
            this.state = state;
        }

        public Peer getPeer() {
            return peer;
        }

        public long getRid() {
            return rid;
        }

        public MessageState getState() {
            return state;
        }
    }

    public static class CounterChanged {
        private Peer peer;
        private int count;

        public CounterChanged(Peer peer, int count) {
            this.peer = peer;
            this.count = count;
        }

        public Peer getPeer() {
            return peer;
        }

        public int getCount() {
            return count;
        }
    }

    public static class Deleted {
        private Peer peer;
        private Message message;

        public Deleted(Peer peer, Message message) {
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

    public static class HistoryLoaded {
        private List<DialogHistory> history;

        public HistoryLoaded(List<DialogHistory> history) {
            this.history = history;
        }

        public List<DialogHistory> getHistory() {
            return history;
        }
    }
}
