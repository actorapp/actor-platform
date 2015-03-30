package im.actor.model.modules.messages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.model.annotation.Verified;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.engine.ListEngine;
import im.actor.model.droidkit.engine.SyncKeyValue;
import im.actor.model.entity.Message;
import im.actor.model.entity.MessageState;
import im.actor.model.entity.Peer;
import im.actor.model.entity.content.AbsContent;
import im.actor.model.modules.Modules;
import im.actor.model.modules.messages.entity.OutUnreadMessage;
import im.actor.model.modules.messages.entity.OutUnreadMessagesStorage;
import im.actor.model.modules.utils.ModuleActor;

/**
 * Actor for managing any Conversation
 * <p></p>
 * Possible bugs
 * 1) Sometimes actor receive read history with old read/receive states.
 * May be we need to update it manually during history load?
 * 2) Sometimes conversation may become out of sync with dialog list.
 * This bug will be auto-heal on any new message.
 */
public class ConversationActor extends ModuleActor {

    private Peer peer;
    private ListEngine<Message> messages;
    private OutUnreadMessagesStorage messagesStorage;
    private ActorRef dialogsActor;
    private SyncKeyValue pendingKeyValue;

    public ConversationActor(Peer peer, Modules messenger) {
        super(messenger);
        this.peer = peer;
        this.pendingKeyValue = messenger.getMessagesModule().getConversationPending();
    }

    @Override
    public void preStart() {
        messages = messages(peer);
        messagesStorage = new OutUnreadMessagesStorage();
        byte[] data = pendingKeyValue.get(peer.getUnuqueId());
        if (data != null) {
            try {
                messagesStorage = OutUnreadMessagesStorage.fromBytes(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dialogsActor = modules().getMessagesModule().getDialogsActor();
    }

    // Messages receive/update

    @Verified
    private void onInMessage(Message message) {
        // Ignore if we already have this message
        if (messages.getValue(message.getEngineId()) != null) {
            return;
        }

        // Adding message
        messages.addOrUpdateItem(message);

        // Updating dialog
        dialogsActor.send(new DialogsActor.InMessage(peer, message));

        // Adding to pending index
        if (message.getSenderId() == myUid()) {
            messagesStorage.getMessages().add(new OutUnreadMessage(message.getRid(), message.getDate()));
            savePending();
        }
    }

    @Verified
    private void onMessageContentUpdated(long rid, AbsContent content) {
        Message message = messages.getValue(rid);
        // Ignore if we already doesn't have this message
        if (message == null) {
            return;
        }

        // Updating message
        messages.addOrUpdateItem(message.changeContent(content));

        // Updating dialog
        dialogsActor.send(new DialogsActor.MessageContentChanged(peer, rid, content));
    }

    @Verified
    private void onMessageSent(long rid, long date) {
        Message msg = messages.getValue(rid);
        // If we have pending message
        if (msg != null && (msg.getMessageState() == MessageState.PENDING)) {

            // Updating pending index
            // Required for correct read state processing
            for (OutUnreadMessage p : messagesStorage.getMessages()) {
                if (p.getRid() == rid) {
                    messagesStorage.getMessages().remove(p);
                    messagesStorage.getMessages().add(new OutUnreadMessage(rid, date));
                    break;
                }
            }
            savePending();

            // Updating message
            messages.addOrUpdateItem(msg
                    .changeDate(date)
                    .changeState(MessageState.SENT));

            // Updating dialog
            dialogsActor.send(new DialogsActor.MessageSent(peer, rid, date));
        }
    }

    @Verified
    private void onMessageError(long rid) {
        Message msg = messages.getValue(rid);
        // If we have pending or sent message
        if (msg != null && (msg.getMessageState() == MessageState.PENDING ||
                msg.getMessageState() == MessageState.SENT)) {

            // Updating message
            messages.addOrUpdateItem(msg
                    .changeState(MessageState.ERROR));

            // Updating dialog
            dialogsActor.send(new DialogsActor.MessageStateChanged(peer, rid,
                    MessageState.ERROR));
        }
    }

    // Read/Receive

    @Verified
    private void onMessagePlainRead(long date) {
        boolean removed = false;
        // Finding all received or sent messages <= date
        for (OutUnreadMessage p : messagesStorage.getMessages().toArray(new OutUnreadMessage[0])) {
            if (p.getDate() <= date) {
                Message msg = messages.getValue(p.getRid());
                if (msg != null && (msg.getMessageState() == MessageState.SENT ||
                        msg.getMessageState() == MessageState.RECEIVED)) {

                    // Updating message
                    messages.addOrUpdateItem(msg
                            .changeState(MessageState.READ));

                    // Updating dialog
                    dialogsActor.send(new DialogsActor.MessageStateChanged(peer, p.getRid(),
                            MessageState.READ));

                    // Removing from pending index
                    removed = true;
                    messagesStorage.getMessages().remove(p);
                }
            }
        }

        // Saving pending index if required
        if (removed) {
            savePending();
        }
    }

    @Verified
    private void onMessagePlainReceived(long date) {
        // Finding all sent messages <= date
        for (OutUnreadMessage p : messagesStorage.getMessages()) {
            if (p.getDate() <= date) {
                Message msg = messages.getValue(p.getRid());
                if (msg != null && msg.getMessageState() == MessageState.SENT) {

                    // Updating message
                    messages.addOrUpdateItem(msg
                            .changeState(MessageState.RECEIVED));

                    // Updating dialog
                    dialogsActor.send(new DialogsActor.MessageStateChanged(peer, p.getRid(),
                            MessageState.RECEIVED));
                }
            }
        }
    }

    @Verified
    private void onMessageEncryptedReceived(long rid) {

        // If we have sent message with this rid
        Message msg = messages.getValue(rid);
        if (msg != null && msg.getMessageState() == MessageState.SENT) {

            // Update message
            messages.addOrUpdateItem(msg
                    .changeState(MessageState.RECEIVED));

            // Update dialog
            dialogsActor.send(new DialogsActor.MessageStateChanged(peer, rid,
                    MessageState.RECEIVED));
        }
    }

    @Verified
    private void onMessageEncryptedRead(long rid) {
        // If we have sent or received message with this rid
        Message msg = messages.getValue(rid);
        if (msg != null && (msg.getMessageState() == MessageState.SENT ||
                msg.getMessageState() == MessageState.RECEIVED)) {

            // Update message
            messages.addOrUpdateItem(msg
                    .changeState(MessageState.READ));

            // Update dialog
            dialogsActor.send(new DialogsActor.MessageStateChanged(peer, rid,
                    MessageState.READ));
        }
    }

    // Deletions

    @Verified
    private void onMessagesDeleted(List<Long> rids) {

        // Removing messages
        long[] rids2 = new long[rids.size()];
        for (int i = 0; i < rids2.length; i++) {
            rids2[i] = rids.get(i);
        }
        messages.removeItems(rids2);

        // Updating dialog
        Message topMessage = messages.getHeadValue();
        dialogsActor.send(new DialogsActor.MessageDeleted(peer, topMessage));
    }

    @Verified
    private void onClearConversation() {
        messages.clear();
        dialogsActor.send(new DialogsActor.ChatClear(peer));
    }

    @Verified
    private void onDeleteConversation() {
        messages.clear();
        dialogsActor.send(new DialogsActor.ChatDelete(peer));
    }

    // History

    @Verified
    private void onHistoryLoaded(List<Message> history) {

        ArrayList<Message> updated = new ArrayList<Message>();
        boolean isPendingChanged = false;

        // Processing all new messages
        for (Message historyMessage : history) {
            // Ignore already present messages
            if (messages.getValue(historyMessage.getEngineId()) != null) {
                continue;
            }

            updated.add(historyMessage);

            // Add unread messages to pending index
            if (historyMessage.getMessageState() == MessageState.SENT) {
                messagesStorage.getMessages().add(new OutUnreadMessage(historyMessage.getRid(), historyMessage.getDate()));
                isPendingChanged = true;
            }
        }

        // Saving pending index if required
        if (isPendingChanged) {
            savePending();
        }

        // Updating messages
        if (updated.size() > 0) {
            messages.addOrUpdateItems(updated);
        }

        // No need to update dialogs: all history messages are always too old
    }

    // Utils

    @Verified
    private void savePending() {
        pendingKeyValue.put(peer.getUnuqueId(), messagesStorage.toByteArray());
    }

    // Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof Message) {
            onInMessage((Message) message);
        } else if (message instanceof MessageContentUpdated) {
            MessageContentUpdated contentUpdated = (MessageContentUpdated) message;
            onMessageContentUpdated(contentUpdated.getRid(), contentUpdated.getContent());
        } else if (message instanceof MessageSent) {
            MessageSent sent = (MessageSent) message;
            onMessageSent(sent.getRid(), sent.getDate());
        } else if (message instanceof MessageError) {
            MessageError messageError = (MessageError) message;
            onMessageError(messageError.getRid());
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
        } else if (message instanceof ClearConversation) {
            onClearConversation();
        } else if (message instanceof DeleteConversation) {
            onDeleteConversation();
        } else if (message instanceof MessagesDeleted) {
            onMessagesDeleted(((MessagesDeleted) message).getRids());
        } else {
            drop(message);
        }
    }

    public static class MessageContentUpdated {
        private long rid;
        private AbsContent content;

        public MessageContentUpdated(long rid, AbsContent content) {
            this.rid = rid;
            this.content = content;
        }

        public long getRid() {
            return rid;
        }

        public AbsContent getContent() {
            return content;
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

    public static class MessageError {
        private long rid;

        public MessageError(long rid) {
            this.rid = rid;
        }

        public long getRid() {
            return rid;
        }
    }

    public static class MessagesDeleted {
        private List<Long> rids;

        public MessagesDeleted(List<Long> rids) {
            this.rids = rids;
        }

        public List<Long> getRids() {
            return rids;
        }
    }

    public static class ClearConversation {

    }

    public static class DeleteConversation {

    }
}