package im.actor.model.modules.messages;

import com.droidkit.actors.Actor;
import com.droidkit.actors.ActorRef;
import im.actor.model.Messenger;
import im.actor.model.entity.Message;
import im.actor.model.entity.MessageState;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PendingMessage;
import im.actor.model.mvvm.KeyValueEngine;
import im.actor.model.mvvm.ListEngine;

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

    private void onInMessage(Message message) {
        if (messages.getValue(message.getListId()) != null) {
            return;
        }

        // Write message
        messages.addOrUpdateItem(message);
        // Updating dialogs
        dialogsActor.send(new DialogsActor.InMessage(peer, message));
    }

    private void onMessageStateChanged(MessageStateChanged stateChanged) {
        Message message = messages.getValue(stateChanged.getRid());
        if (message == null) {
            return;
        }
        messages.addOrUpdateItem(message.changeState(stateChanged.messageState));
        // TODO: Send to dialogs
    }

    private void onMessageEncryptedRead(long rid) {

    }

    private void onMessagePlainRead(long date) {

    }

    private void onMessageEncryptedReceived(long rid) {

    }

    private void onMessagePlainReceived(long date) {

    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof Message) {
            onInMessage((Message) message);
        } else if (message instanceof MessageStateChanged) {
            onMessageStateChanged((MessageStateChanged) message);
        } else if (message instanceof MessageRead) {
            onMessagePlainRead(((MessageRead) message).getDate());
        } else if (message instanceof MessageEncryptedRead) {
            onMessageEncryptedRead(((MessageEncryptedRead) message).getRid());
        } else if (message instanceof MessageReceived) {
            onMessagePlainReceived(((MessageReceived) message).getDate());
        } else if (message instanceof MessageEncryptedReceived) {
            onMessageEncryptedReceived(((MessageEncryptedReceived) message).getRid());
        } else {
            drop(message);
        }
    }

    public static class MessageStateChanged {
        private long rid;
        private MessageState messageState;

        public MessageStateChanged(long rid, MessageState messageState) {
            this.rid = rid;
            this.messageState = messageState;
        }

        public long getRid() {
            return rid;
        }

        public MessageState getMessageState() {
            return messageState;
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
}