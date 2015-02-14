package im.actor.model.modules.messages;

import im.actor.model.droidkit.actors.Actor;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.Messenger;
import im.actor.model.entity.Message;
import im.actor.model.entity.MessageState;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PendingMessage;
import im.actor.model.mvvm.KeyValueEngine;
import im.actor.model.mvvm.ListEngine;

import java.util.List;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class ConversationActor extends Actor {

    private Messenger messenger;
    private Peer peer;
    private ListEngine<Message> messages;
    private KeyValueEngine<PendingMessage> pendingMessages;
    private ActorRef dialogsActor;

    public ConversationActor(Peer peer, Messenger messenger) {
        this.peer = peer;
        this.messenger = messenger;
    }

    @Override
    public void preStart() {
        messages = messenger.getMessages(peer);
        // TODO: Replace
        pendingMessages = messenger.getConfiguration().getEnginesFactory().pendingMessages(peer);
        dialogsActor = messenger.getMessagesModule().getDialogsActor();

    }

    private void onHistoryLoaded(List<Message> history) {

    }

    private void onInMessage(Message message) {
        if (messages.getValue(message.getListId()) != null) {
            return;
        }

        // Write message
        messages.addOrUpdateItem(message);
        // Updating dialogs
        dialogsActor.send(new DialogsActor.InMessage(peer, message));

        if (message.getSenderId() == messenger.myUid()) {
            pendingMessages.addOrUpdateItem(new PendingMessage(message.getRid(), message.getDate()));
        }
    }

    private void onMessagePlainRead(long date) {
        for (PendingMessage p : pendingMessages.getAll()) {
            if (p.getDate() <= date) {
                Message msg = messages.getValue(p.getRid());
                if (msg != null && (msg.getMessageState() == MessageState.SENT ||
                        msg.getMessageState() == MessageState.RECEIVED)) {
                    messages.addOrUpdateItem(msg
                            .changeState(MessageState.READ));
                    dialogsActor.send(new DialogsActor.MessageStateChanged(peer, p.getRid(),
                            MessageState.READ));
                }
                pendingMessages.removeItem(p.getRid());
            }
        }
    }

    private void onMessagePlainReceived(long date) {
        for (PendingMessage p : pendingMessages.getAll()) {
            if (p.getDate() <= date) {
                Message msg = messages.getValue(p.getRid());
                if (msg != null && msg.getMessageState() == MessageState.SENT) {
                    messages.addOrUpdateItem(msg
                            .changeState(MessageState.RECEIVED));
                    dialogsActor.send(new DialogsActor.MessageStateChanged(peer, p.getRid(),
                            MessageState.RECEIVED));
                }
            }
        }
    }

    private void onMessageEncryptedReceived(long rid) {

    }

    private void onMessageEncryptedRead(long rid) {

    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof Message) {
            onInMessage((Message) message);
        } else if (message instanceof MessageRead) {
            onMessagePlainRead(((MessageRead) message).getDate());
        } else if (message instanceof MessageEncryptedRead) {
            onMessageEncryptedRead(((MessageEncryptedRead) message).getRid());
        } else if (message instanceof MessageReceived) {
            onMessagePlainReceived(((MessageReceived) message).getDate());
        } else if (message instanceof MessageEncryptedReceived) {
            onMessageEncryptedReceived(((MessageEncryptedReceived) message).getRid());
        } else if (message instanceof HistoryLoaded) {
            onHistoryLoaded(((HistoryLoaded) message).getMessages());
        } else {
            drop(message);
        }
    }

    public static class HistoryLoaded {
        private List<Message> messages;

        public HistoryLoaded(List<Message> messages) {
            this.messages = messages;
        }

        public List<Message> getMessages() {
            return messages;
        }
    }

    public static class MessageReceived {
        private long date;

        public MessageReceived(long date) {
            this.date = date;
        }

        public long getDate() {
            return date;
        }
    }

    public static class MessageEncryptedReceived {
        private long rid;

        public MessageEncryptedReceived(long rid) {
            this.rid = rid;
        }

        public long getRid() {
            return rid;
        }
    }

    public static class MessageRead {
        private long date;

        public MessageRead(long date) {
            this.date = date;
        }

        public long getDate() {
            return date;
        }
    }

    public static class MessageEncryptedRead {
        private long rid;

        public MessageEncryptedRead(long rid) {
            this.rid = rid;
        }

        public long getRid() {
            return rid;
        }
    }

    public static class MessageSent {
        private long rid;
        private long date;

        public MessageSent(long rid, long date) {
            this.rid = rid;
            this.date = date;
        }

        public long getDate() {
            return date;
        }

        public long getRid() {
            return rid;
        }
    }

    public static class MessageDeleted {
        private List<Long> rids;

        public MessageDeleted(List<Long> rids) {
            this.rids = rids;
        }

        public List<Long> getRids() {
            return rids;
        }
    }
}