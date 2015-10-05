/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.messages;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.entity.Message;
import im.actor.core.entity.MessageState;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.entity.content.DocumentContent;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.annotations.Verified;
import im.actor.runtime.storage.IndexStorage;
import im.actor.runtime.storage.ListEngine;

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

    private final String IN_READ_STATE_PREF;
    private final String OUT_READ_STATE_PREF;
    private final String OUT_RECEIVE_STATE_PREF;

    private Peer peer;
    private boolean isHiddenPeer = false;

    private ListEngine<Message> messages;
    private ListEngine<Message> docs;
    private IndexStorage outPendingIndex;
    private IndexStorage inPendingIndex;
    private ActorRef dialogsActor;
    private long inReadState;
    private long outReadState;
    private long outReceiveState;

    public ConversationActor(Peer peer, ModuleContext context) {
        super(context);
        this.peer = peer;
        this.IN_READ_STATE_PREF = "chat_state." + peer + ".in_read";
        this.OUT_READ_STATE_PREF = "chat_state." + peer + ".out_read";
        this.OUT_RECEIVE_STATE_PREF = "chat_state." + peer + ".out_receive";
    }

    @Override
    public void preStart() {
        messages = context().getMessagesModule().getConversationEngine(peer);
        docs = context().getMessagesModule().getConversationDocsEngine(peer);

        dialogsActor = context().getMessagesModule().getDialogsActor();
        outPendingIndex = Storage.createIndex("out_pending_" + peer.getPeerType() + "_" + peer.getPeerId());
        inPendingIndex = Storage.createIndex("in_pending_" + peer.getPeerType() + "_" + peer.getPeerId());

        inReadState = context().getPreferences().getLong(IN_READ_STATE_PREF, 0);
        outReadState = context().getPreferences().getLong(OUT_READ_STATE_PREF, 0);
        outReceiveState = context().getPreferences().getLong(OUT_RECEIVE_STATE_PREF, 0);

        if (peer.getPeerType() == PeerType.GROUP) {
            isHiddenPeer = getGroup(peer.getPeerId()).isHidden();
        }
    }

    // Messages receive/update

    @Verified
    private void onInMessages(ArrayList<Message> inMessages) {

        // Prepare messages
        Message topMessage = null;
        ArrayList<Message> updated = new ArrayList<Message>();
        ArrayList<Message> updatedDocs = new ArrayList<Message>();
        for (Message m : inMessages) {
            if (m.getSenderId() == myUid()) {
                // Force set message state if server out message
                if (m.isOnServer()) {
                    if (m.getSortDate() <= outReadState) {
                        m = m.changeState(MessageState.READ);
                    } else if (m.getSortDate() <= outReceiveState) {
                        m = m.changeState(MessageState.RECEIVED);
                    } else {
                        m = m.changeState(MessageState.SENT);
                    }
                }
            }

            if (m.isOnServer()) {
                if (topMessage == null) {
                    topMessage = m;
                } else {
                    if (topMessage.getSortDate() < m.getSortDate()) {
                        topMessage = m;
                    }
                }
            }

            updated.add(m);
            if (m.getContent() instanceof DocumentContent) {
                updatedDocs.add(m);
            }
        }

        // Adding message
        messages.addOrUpdateItems(updated);
        docs.addOrUpdateItems(updatedDocs);

        for (Message m : updated) {
            if (m.getSenderId() == myUid()) {
                // Adding to unread index if message is unread
                if (m.isOnServer() && m.getMessageState() != MessageState.READ) {
                    outPendingIndex.put(m.getRid(), m.getDate());
                }
            } else {
                // Detecting if message already read
                if (m.getSortDate() > inReadState) {
                    // Writing to income unread storage
                    inPendingIndex.put(m.getRid(), m.getDate());
                }
            }
        }

        // Update dialogs
        if (topMessage != null) {
            if (!isHiddenPeer) {
                dialogsActor.send(new DialogsActor.InMessage(peer, topMessage, inPendingIndex.getCount()));
            }
        }
    }

    @Verified
    private void onInMessage(Message message) {

        // Ignore if we already have this message
        // UPDATE: Changed behaviour to providing faster implementation
        // if (messages.getValue(message.getEngineId()) != null) {
        //     return;
        // }

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
        if (message.getContent() instanceof DocumentContent) {
            docs.addOrUpdateItem(message);
        }

        // Updating dialog if on server
        if (message.isOnServer()) {
            if (message.getSenderId() == myUid()) {
                // Adding to unread index if message is unread
                if (message.isOnServer() && message.getMessageState() != MessageState.READ) {
                    outPendingIndex.put(message.getRid(), message.getDate());
                }
            } else {
                // Detecting if message already read
                if (message.getSortDate() > inReadState) {
                    // Writing to income unread storage
                    inPendingIndex.put(message.getRid(), message.getDate());
                }
            }

            if (!isHiddenPeer) {
                dialogsActor.send(new DialogsActor.InMessage(peer, message, inPendingIndex.getCount()));
            }
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
        if (updatedMsg.getContent() instanceof DocumentContent) {
            docs.addOrUpdateItem(updatedMsg);
        } else {
            docs.removeItem(rid);
        }

        if (!isHiddenPeer) {
            // Updating dialog
            dialogsActor.send(new DialogsActor.MessageContentChanged(peer, rid, content));
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
            if (updatedMsg.getContent() instanceof DocumentContent) {
                docs.addOrUpdateItem(updatedMsg);
            }

            if (!isHiddenPeer) {
                // Updating dialog
                dialogsActor.send(new DialogsActor.InMessage(peer, updatedMsg, inPendingIndex.getCount()));
            }

            // Updating pending index
            if (state != MessageState.READ) {
                outPendingIndex.put(rid, date);
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

            if (updatedMsg.getContent() instanceof DocumentContent) {
                docs.addOrUpdateItem(updatedMsg);
            }

            if (!isHiddenPeer) {
                // Updating dialog
                dialogsActor.send(new DialogsActor.MessageStateChanged(peer, rid,
                        MessageState.ERROR));
            }
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

        List<Long> res = outPendingIndex.removeBeforeValue(date);
        if (res.size() > 0) {
            long minRid = -1;
            long minDate = Long.MAX_VALUE;
            ArrayList<Message> updated = new ArrayList<Message>();
            for (Long ref : res) {
                Message msg = messages.getValue(ref);
                if (msg != null && msg.isReceivedOrSent()) {
                    if (msg.getDate() < minDate) {
                        minDate = msg.getDate();
                        minRid = ref;
                    }

                    updated.add(msg.changeState(MessageState.READ));
                }
            }

            if (updated.size() > 0) {
                messages.addOrUpdateItems(updated);
            }

            if (minRid != -1) {
                if (!isHiddenPeer) {
                    dialogsActor.send(new DialogsActor.MessageStateChanged(peer, minRid, MessageState.READ));
                }
            }
        }
    }

    @Verified
    private void onMessageReceived(long date) {
        if (date <= outReceiveState) {
            return;
        }
        outReceiveState = date;
        preferences().putLong(OUT_RECEIVE_STATE_PREF, date);

        List<Long> res = outPendingIndex.findBeforeValue(date);

        if (res.size() > 0) {
            long maxRid = -1;
            long maxDate = Long.MIN_VALUE;
            ArrayList<Message> updated = new ArrayList<Message>();

            for (Long ref : res) {
                Message msg = messages.getValue(ref);
                if (msg != null && msg.isSent()) {
                    if (msg.getDate() > maxDate) {
                        maxDate = msg.getDate();
                        maxRid = ref;
                    }

                    updated.add(msg.changeState(MessageState.RECEIVED));
                }
            }

            if (updated.size() > 0) {
                messages.addOrUpdateItems(updated);
            }

            if (maxRid != -1) {
                if (!isHiddenPeer) {
                    dialogsActor.send(new DialogsActor.MessageStateChanged(peer, maxRid, MessageState.RECEIVED));
                }
            }
        }
    }

    @Verified
    private void onMessageReadByMe(long date) {
        if (date < inReadState) {
            return;
        }
        inReadState = date;
        preferences().putLong(IN_READ_STATE_PREF, date);

        inPendingIndex.removeBeforeValue(date);

        if (!isHiddenPeer) {
            dialogsActor.send(new DialogsActor.CounterChanged(peer, inPendingIndex.getCount()));
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
        docs.removeItems(rids2);

        inPendingIndex.remove(rids);
        outPendingIndex.remove(rids);

        // Updating dialog
        if (!isHiddenPeer) {
            dialogsActor.send(new DialogsActor.MessageDeleted(peer, messages.getHeadValue()));
        }
    }

    @Verified
    private void onClearConversation() {
        messages.clear();
        docs.clear();
        inPendingIndex.clear();
        outPendingIndex.clear();
        dialogsActor.send(new DialogsActor.ChatClear(peer));
    }

    @Verified
    private void onDeleteConversation() {
        messages.clear();
        docs.clear();
        inPendingIndex.clear();
        outPendingIndex.clear();
        dialogsActor.send(new DialogsActor.ChatDelete(peer));
    }

    // History

    @Verified
    private void onHistoryLoaded(List<Message> history) {

        ArrayList<Message> updated = new ArrayList<Message>();
        ArrayList<Message> updatedDocs = new ArrayList<Message>();

        // Processing all new messages
        for (Message historyMessage : history) {
            // Ignore already present messages
            if (messages.getValue(historyMessage.getEngineId()) != null) {
                continue;
            }

            updated.add(historyMessage);
            if (historyMessage.getContent() instanceof DocumentContent) {
                updatedDocs.add(historyMessage);
            }
        }

        // Updating messages
        if (updated.size() > 0) {
            messages.addOrUpdateItems(updated);
        }

        if (updatedDocs.size() > 0) {
            docs.addOrUpdateItems(updatedDocs);
        }

        // No need to update dialogs: all history messages are always too old
    }

    // Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof Message) {
            onInMessage((Message) message);
        } else if (message instanceof Messages) {
            onInMessages(((Messages) message).getMessages());
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
        } else if (message instanceof MessageReadByMe) {
            onMessageReadByMe(((MessageReadByMe) message).getDate());
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

    public static class MessageReadByMe {
        private long date;

        public MessageReadByMe(long date) {
            this.date = date;
        }

        public long getDate() {
            return date;
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

    public static class Messages {
        private ArrayList<Message> messages;

        public Messages(ArrayList<Message> messages) {
            this.messages = messages;
        }

        public ArrayList<Message> getMessages() {
            return messages;
        }
    }
}