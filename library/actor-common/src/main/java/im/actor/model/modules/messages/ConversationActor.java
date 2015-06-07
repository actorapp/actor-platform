/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

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

        if (message.getMessageState() != MessageState.PENDING && message.getMessageState() != MessageState.ERROR) {
            // Updating dialog if not pending
            dialogsActor.send(new DialogsActor.InMessage(peer, message));
        }

        // Adding to pending index
        if (message.getSenderId() == myUid()) {
            messagesStorage.getMessages().add(new OutUnreadMessage(message.getRid(), message.getDate()));
            savePending();
        }
    }

    @Verified
    private void onInMessageOverride(Message message) {
        // Check if we already have this message
        boolean isOverride = messages.getValue(message.getEngineId()) != null;

        // Adding message
        messages.addOrUpdateItem(message);

        if (message.getMessageState() != MessageState.PENDING && message.getMessageState() != MessageState.ERROR) {
            // Updating dialog if not pending
            dialogsActor.send(new DialogsActor.InMessage(peer, message));
        }

        // Adding to pending index
        if (message.getSenderId() == myUid() && !isOverride) {
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
        Message updatedMsg = message.changeContent(content);
        messages.addOrUpdateItem(updatedMsg);

        // Updating dialog
        dialogsActor.send(new DialogsActor.MessageContentChanged(peer, rid, content));
    }

    @Verified
    @Deprecated
    private void onMessageDateChange(long rid, long date) {
        Message msg = messages.getValue(rid);
        // If we have sent message
        if (msg != null && msg.isOnServer()) {
            Message updatedMsg = msg
                    .changeAllDate(date)
                    .changeState(MessageState.SENT);
            messages.addOrUpdateItem(updatedMsg);
        }
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
            Message updatedMsg = msg
                    .changeAllDate(date)
                    .changeState(MessageState.SENT);
            messages.addOrUpdateItem(updatedMsg);

            // Updating dialog
            dialogsActor.send(new DialogsActor.InMessage(peer, updatedMsg));
        }
    }

    @Verified
    private void onMessageError(long rid) {
        Message msg = messages.getValue(rid);
        // If we have pending or sent message
        if (msg != null && msg.isPendingOrSent()) {

            // Updating message
            Message updatedMsg = msg
                    .changeState(MessageState.ERROR);
            messages.addOrUpdateItem(updatedMsg);

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
        ArrayList<OutUnreadMessage> messagesStorageMessages = messagesStorage.getMessages();
        for (OutUnreadMessage p : messagesStorageMessages.toArray(new OutUnreadMessage[messagesStorageMessages.size()])) {
            if (p.getDate() <= date) {
                Message msg = messages.getValue(p.getRid());
                if (msg != null && msg.isReceivedOrSent()) {

                    // Updating message
                    Message updatedMsg = msg
                            .changeState(MessageState.READ);
                    messages.addOrUpdateItem(updatedMsg);

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
                if (msg != null && msg.isSent()) {

                    // Updating message
                    Message updatedMsg = msg
                            .changeState(MessageState.RECEIVED);
                    messages.addOrUpdateItem(updatedMsg);

                    // Updating dialog
                    dialogsActor.send(new DialogsActor.MessageStateChanged(peer, p.getRid(),
                            MessageState.RECEIVED));
                }
            }
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
        } else if (message instanceof MessageReceived) {
            onMessagePlainReceived(((MessageReceived) message).getDate());
        } else if (message instanceof HistoryLoaded) {
            onHistoryLoaded(((HistoryLoaded) message).getMessages());
        } else if (message instanceof ClearConversation) {
            onClearConversation();
        } else if (message instanceof DeleteConversation) {
            onDeleteConversation();
        } else if (message instanceof MessagesDeleted) {
            onMessagesDeleted(((MessagesDeleted) message).getRids());
        } else if (message instanceof MessageDateChange) {
            onMessageDateChange(((MessageDateChange) message).getRid(), ((MessageDateChange) message).getDate());
        } else {
            drop(message);
        }
    }

    @Deprecated
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

    public static class MessageRead {
        private long date;

        public MessageRead(long date) {
            this.date = date;
        }

        public long getDate() {
            return date;
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

    @Deprecated
    public static class MessageDateChange {
        private long rid;
        private long date;

        public MessageDateChange(long rid, long date) {
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