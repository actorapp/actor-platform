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
import im.actor.model.modules.messages.entity.MessageRef;
import im.actor.model.modules.messages.entity.MessagesStorage;
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

    private final String OUT_READ_STATE_PREF;
    private final String OUT_RECEIVE_STATE_PREF;

    private Peer peer;
    private ListEngine<Message> messages;
    private SyncKeyValue outPendingKeyValue;
    private SyncKeyValue inPendingKeyValue;
    private MessagesStorage outMessagesStorage;
    private MessagesStorage inMessagesStorage;
    private ActorRef dialogsActor;
    private long inReadState;
    private long outReadState;
    private long outReceiveState;

    public ConversationActor(Peer peer, Modules messenger) {
        super(messenger);
        this.peer = peer;
        this.outPendingKeyValue = messenger.getMessagesModule().getConversationPendingOut();
        this.inPendingKeyValue = messenger.getMessagesModule().getConversationPendingIn();
        this.OUT_READ_STATE_PREF = "chat_state." + peer + ".out_read";
        this.OUT_RECEIVE_STATE_PREF = "chat_state." + peer + ".out_receive";
    }

    @Override
    public void preStart() {
        messages = messages(peer);
        dialogsActor = modules().getMessagesModule().getDialogsActor();
        inReadState = modules().getMessagesModule().loadReadState(peer);
        inMessagesStorage = new MessagesStorage();
        outMessagesStorage = new MessagesStorage();
        outReadState = modules().getPreferences().getLong(OUT_READ_STATE_PREF, 0);
        outReceiveState = modules().getPreferences().getLong(OUT_RECEIVE_STATE_PREF, 0);

        // Loading pending message refs
        try {
            byte[] data = inPendingKeyValue.get(peer.getUnuqueId());
            if (data != null) {
                inMessagesStorage = MessagesStorage.fromBytes(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            byte[] data = outPendingKeyValue.get(peer.getUnuqueId());
            if (data != null) {
                outMessagesStorage = MessagesStorage.fromBytes(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Messages receive/update

    @Verified
    private void onInMessage(Message message) {
        // Ignore if we already have this message
        if (messages.getValue(message.getEngineId()) != null) {
            return;
        }

        if (message.getSenderId() == myUid()) {
            // Force set message state if server out message
            if (message.isOnServer()) {
                if (message.getSortDate() <= outReadState) {
                    message = message.changeState(MessageState.READ);
                } else if (message.getSortDate() <= outReceiveState) {
                    message = message.changeState(MessageState.RECEIVED);
                } else {
                    message = message.changeState(MessageState.SENT);
                }
            }
        }

        // Adding message
        messages.addOrUpdateItem(message);

        // Updating dialog if on server
        if (message.isOnServer()) {
            if (message.getSenderId() == myUid()) {
                // Adding to unread index if message is unread
                if (message.isOnServer() && message.getMessageState() != MessageState.READ) {
                    outMessagesStorage.addOrUpdate(message.getRid(), message.getDate());
                    saveOutPending();
                }
            } else {
                // Detecting if message already read
                if (message.getSortDate() <= inReadState) {
                    return;
                }

                // ContentDescription contentDescription = ContentDescription.fromContent(message.getContent());
            }

            dialogsActor.send(new DialogsActor.InMessage(peer, message, inMessagesStorage.getCount()));
        }


//        // Detecting if message already read
//        long readState = modules().getMessagesModule().loadReadState(peer);
//        if (sortingDate <= readState) {
//            // Already read
//            return;
//        }
//
//        // TODO: ???????
//        // Notify notification actor
//        if (contentDescription != null) {
//            modules().getNotifications().onInMessage(peer, senderUid, sortingDate, contentDescription, hasCurrentUserMention,
//                    false);
//        }
//
//        // Saving unread message to storage
//        HashSet<UnreadMessage> unread = messagesStorage.getUnread(peer);
//        unread.add(new UnreadMessage(peer, rid, sortingDate));
//        saveStorage();
//
//        // Updating counter
//        modules().getMessagesModule().getDialogsActor()
//                .send(new DialogsActor.CounterChanged(peer, unread.size()));
    }

//    @Verified
//    private void onInMessageOverride(Message message) {
//        // Check if we already have this message
//        boolean isOverride = messages.getValue(message.getEngineId()) != null;
//
//        // Adding message
//        messages.addOrUpdateItem(message);
//
//        if (message.getMessageState() != MessageState.PENDING && message.getMessageState() != MessageState.ERROR) {
//            // Updating dialog if not pending
//            dialogsActor.send(new DialogsActor.InMessage(peer, message));
//        }
//
//        // Adding to pending index
//        if (message.getSenderId() == myUid() && !isOverride) {
//            messagesStorage.getMessages().add(new MessageRef(message.getRid(), message.getDate()));
//            savePending();
//        }
//    }

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

            MessageState state;
            if (date <= outReadState) {
                state = MessageState.READ;
            } else if (date <= outReceiveState) {
                state = MessageState.RECEIVED;
            } else {
                state = MessageState.SENT;
            }

            // Updating message
            Message updatedMsg = msg
                    .changeAllDate(date)
                    .changeState(state);
            messages.addOrUpdateItem(updatedMsg);

            // Updating dialog
            dialogsActor.send(new DialogsActor.InMessage(peer, updatedMsg, inMessagesStorage.getCount()));

            // Updating pending index
            if (state != MessageState.READ) {
                outMessagesStorage.addOrUpdate(rid, date);
            }
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
    private void onMessageRead(long date) {
        if (date <= outReadState) {
            return;
        }
        outReadState = date;
        preferences().putLong(OUT_READ_STATE_PREF, date);

        ArrayList<MessageRef> res = outMessagesStorage.removeBeforeDate(date);
        if (res.size() > 0) {
            long minRid = -1;
            long minDate = Long.MAX_VALUE;
            ArrayList<Message> updated = new ArrayList<Message>();
            for (MessageRef ref : res) {
                Message msg = messages.getValue(ref.getRid());
                if (msg != null && msg.isReceivedOrSent()) {
                    if (ref.getDate() < minDate) {
                        minDate = ref.getDate();
                        minRid = ref.getRid();
                    }

                    updated.add(msg.changeState(MessageState.READ));
                }
            }

            if (updated.size() > 0) {
                messages.addOrUpdateItems(updated);
            }

            if (minRid != -1) {
                dialogsActor.send(new DialogsActor.MessageStateChanged(peer, minRid, MessageState.READ));
            }

            saveOutPending();
        }
    }

    @Verified
    private void onMessageReceived(long date) {
        if (date <= outReceiveState) {
            return;
        }
        outReceiveState = date;
        preferences().putLong(OUT_RECEIVE_STATE_PREF, date);

        ArrayList<MessageRef> res = outMessagesStorage.findBeforeDate(date);

        if (res.size() > 0) {
            long minRid = -1;
            long minDate = Long.MAX_VALUE;
            ArrayList<Message> updated = new ArrayList<Message>();

            for (MessageRef ref : res) {
                Message msg = messages.getValue(ref.getRid());
                if (msg != null && msg.isSent()) {
                    if (ref.getDate() < minDate) {
                        minDate = ref.getDate();
                        minRid = ref.getRid();
                    }

                    updated.add(msg.changeState(MessageState.RECEIVED));
                }
            }

            if (updated.size() > 0) {
                messages.addOrUpdateItems(updated);
            }

            if (minRid != -1) {
                dialogsActor.send(new DialogsActor.MessageStateChanged(peer, minRid, MessageState.RECEIVED));
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

        // TODO: Process pendings

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
        boolean isOutPendingChanged = false;

        // Processing all new messages
        for (Message historyMessage : history) {
            // Ignore already present messages
            if (messages.getValue(historyMessage.getEngineId()) != null) {
                continue;
            }

            updated.add(historyMessage);

            // Add unread messages to pending index
            if (historyMessage.getMessageState() != MessageState.READ) {
                outMessagesStorage.addOrUpdate(historyMessage.getRid(), historyMessage.getDate());
                isOutPendingChanged = true;
            }
        }

        // Saving pending index if required
        if (isOutPendingChanged) {
            saveOutPending();
        }

        // Updating messages
        if (updated.size() > 0) {
            messages.addOrUpdateItems(updated);
        }

        // No need to update dialogs: all history messages are always too old
    }

    // Utils

    @Verified
    private void saveOutPending() {
        outPendingKeyValue.put(peer.getUnuqueId(), outMessagesStorage.toByteArray());
    }

    @Verified
    private void saveInPending() {
        inPendingKeyValue.put(peer.getUnuqueId(), inMessagesStorage.toByteArray());
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
            onMessageRead(((MessageRead) message).getDate());
        } else if (message instanceof MessageReceived) {
            onMessageReceived(((MessageReceived) message).getDate());
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