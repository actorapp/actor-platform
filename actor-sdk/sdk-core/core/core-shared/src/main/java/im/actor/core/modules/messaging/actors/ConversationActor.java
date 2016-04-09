/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.messaging.actors;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.entity.ConversationState;
import im.actor.core.entity.Message;
import im.actor.core.entity.MessageState;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.Reaction;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.entity.content.DocumentContent;
import im.actor.core.modules.ModuleContext;
import im.actor.core.events.AppVisibleChanged;
import im.actor.core.modules.ModuleActor;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.annotations.Verified;
import im.actor.runtime.eventbus.Event;
import im.actor.runtime.storage.IndexEngine;
import im.actor.runtime.storage.IndexStorage;
import im.actor.runtime.storage.KeyValueEngine;
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
    private final String IN_READ_STATE_NEW_PREF;
    private final String OUT_READ_STATE_PREF;
    private final String OUT_RECEIVE_STATE_PREF;

    private Peer peer;
    private long peerUniqueId;
    private boolean isHiddenPeer = false;

    private KeyValueEngine<ConversationState> conversationState;
    private ListEngine<Message> messages;

    private IndexStorage inPendingIndex;
    private ActorRef dialogsActor;
    private ActorRef dialogsGroupedActor;
    private ActorRef readerActor;

    private ConversationState state;

    private boolean isConversationVisible = false;
    private boolean isAppVisible = false;

    public ConversationActor(Peer peer, ModuleContext context) {
        super(context);
        this.peer = peer;
        this.peerUniqueId = peer.getUnuqueId();

        this.IN_READ_STATE_PREF = "chat_state." + peer + ".in_read";
        this.IN_READ_STATE_NEW_PREF = "chat_state." + peer + ".in_read_new";
        this.OUT_READ_STATE_PREF = "chat_state." + peer + ".out_read";
        this.OUT_RECEIVE_STATE_PREF = "chat_state." + peer + ".out_receive";
    }

    @Override
    public void preStart() {
        conversationState = context().getMessagesModule().getConversationStates().getEngine();
        messages = context().getMessagesModule().getConversationEngine(peer);
        readerActor = context().getMessagesModule().getOwnReadActor();

        dialogsActor = context().getMessagesModule().getDialogsActor();
        if (context().getConfiguration().isEnabledGroupedChatList()) {
            dialogsGroupedActor = context().getMessagesModule().getDialogsGroupedActor();
        }

        if (peer.getPeerType() == PeerType.GROUP) {
            isHiddenPeer = getGroup(peer.getPeerId()).isHidden();
        }
        subscribe(AppVisibleChanged.EVENT);


        //
        // Read States
        //

        inPendingIndex = new IndexEngine(Storage.createIndex("in_pending_" + peer.getPeerType() + "_" + peer.getPeerId()));

        long inReadState = context().getPreferences().getLong(IN_READ_STATE_PREF, 0);
        long inMaxDate = context().getPreferences().getLong(IN_READ_STATE_NEW_PREF, 0);
        long outReadState = context().getPreferences().getLong(OUT_READ_STATE_PREF, 0);
        long outReceiveState = context().getPreferences().getLong(OUT_RECEIVE_STATE_PREF, 0);

        // Migration of Read States
        state = conversationState.getValue(peerUniqueId);
        boolean isChanged = false;
        if (state.getInReadDate() < inReadState) {
            state = state.changeInReadDate(inReadState);
            isChanged = true;
        }
        if (state.getInMaxMessageDate() < inMaxDate) {
            state = state.changeInMaxDate(inMaxDate);
            isChanged = true;
        }
        if (state.getOutReadDate() < outReadState) {
            state = state.changeOutReadDate(outReadState);
            isChanged = true;
        }
        if (state.getOutReceiveDate() < outReceiveState) {
            state = state.changeOutReceiveDate(outReceiveState);
            isChanged = true;
        }
        if (isChanged) {
            conversationState.addOrUpdateItem(state);
        }
    }

    // Visibility state

    private void onConversationVisible() {
        isConversationVisible = true;

        if (isConversationAutoRead()) {
            checkReadState(true);
        }
    }

    private void onConversationHidden() {
        isConversationVisible = false;
    }

    private void onAppVisible() {
        isAppVisible = true;

        if (isConversationAutoRead()) {
            checkReadState(true);
        }
    }

    private void onAppHidden() {
        isAppVisible = false;
    }

    private boolean isConversationAutoRead() {
        return isAppVisible && isConversationVisible;
    }

    // Messages receive/update

    @Verified
    private void onInMessages(ArrayList<Message> inMessages) {

        // Prepare messages
        Message topMessage = null;
        ArrayList<Message> updated = new ArrayList<>();
        for (Message m : inMessages) {
            if (m.isOnServer()) {
                if (topMessage == null || topMessage.getSortDate() < m.getSortDate()) {
                    topMessage = m;
                }
            }

            updated.add(m);
        }

        // Adding message
        messages.addOrUpdateItems(updated);

        for (Message m : updated) {
            if (m.getSenderId() != myUid()) {

                if (m.getSortDate() > state.getInMaxMessageDate()) {
                    state = state.changeInMaxDate(m.getSortDate());
                    conversationState.addOrUpdateItem(state);
                }

                // Detecting if message already read
                if (m.getSortDate() > state.getInReadDate()) {
                    // Writing to income unread storage
                    inPendingIndex.put(m.getRid(), m.getDate());
                }
            }
        }

        // Reading messages
        if (isConversationAutoRead()) {
            checkReadState(false);
        }

        // Update dialogs
        if (topMessage != null) {
            if (!isHiddenPeer) {
                dialogsActor.send(new DialogsActor.InMessage(peer, topMessage, inPendingIndex.getCount()));
            }
            if (dialogsGroupedActor != null) {
                dialogsGroupedActor.send(new ActiveDialogsActor.CounterChanged(peer, inPendingIndex.getCount()));
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

        // Adding message
        messages.addOrUpdateItem(message);

        // Updating dialog if on server
        if (message.isOnServer()) {

            if (message.getSenderId() != myUid()) {

                if (message.getSortDate() > state.getInMaxMessageDate()) {
                    state = state.changeInMaxDate(message.getSortDate());
                    conversationState.addOrUpdateItem(state);
                }

                // Detecting if message already read
                if (message.getSortDate() > state.getInReadDate()) {
                    // Writing to income unread storage
                    inPendingIndex.put(message.getRid(), message.getDate());
                }
            }

            if (isConversationAutoRead()) {
                checkReadState(false);
            }

            if (!isHiddenPeer) {
                dialogsActor.send(new DialogsActor.InMessage(peer, message, inPendingIndex.getCount()));
            }
            if (dialogsGroupedActor != null) {
                dialogsGroupedActor.send(new ActiveDialogsActor.CounterChanged(peer, inPendingIndex.getCount()));
            }
        }
    }

    private void onConversationLoaded() {
        ConversationState state = conversationState.getValue(peer.getUnuqueId());
        if (!state.isLoaded()) {
            conversationState.addOrUpdateItem(state
                    .changeIsLoaded(true));
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
        content.incrementUpdatedCounter(message.getContent().getUpdatedCounter());

        Message updatedMsg = message.changeContent(content);
        messages.addOrUpdateItem(updatedMsg);

        if (!isHiddenPeer) {
            // Updating dialog
            dialogsActor.send(new DialogsActor.MessageContentChanged(peer, rid, content));
        }
    }

    @Verified
    private void onMessageReactionsUpdated(long rid, ArrayList<Reaction> reactions) {
        Message message = messages.getValue(rid);
        // Ignore if we already doesn't have this message
        if (message == null) {
            return;
        }

        // Updating message
        Message updatedMsg = message.changeReactions(reactions);
        messages.addOrUpdateItem(updatedMsg);
    }

    @Verified
    private void onMessageSent(long rid, long date) {
        Message msg = messages.getValue(rid);
        // If we have pending message
        if (msg != null && (msg.getMessageState() == MessageState.PENDING)) {

            // Updating message
            Message updatedMsg = msg
                    .changeAllDate(date)
                    .changeState(MessageState.SENT);
            messages.addOrUpdateItem(updatedMsg);

            if (!isHiddenPeer) {
                // Updating dialog
                dialogsActor.send(new DialogsActor.InMessage(peer, updatedMsg, inPendingIndex.getCount()));
                if (dialogsGroupedActor != null) {
                    dialogsGroupedActor.send(new ActiveDialogsActor.CounterChanged(peer, inPendingIndex.getCount()));
                }
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

            if (dialogsGroupedActor != null) {
                dialogsGroupedActor.send(new ActiveDialogsActor.CounterChanged(peer, inPendingIndex.getCount()));
            }
        }
    }

    // Read/Receive

    @Verified
    private void onMessageRead(long date) {
        if (date <= state.getOutReadDate()) {
            return;
        }
        state = state.changeOutReadDate(date);
        conversationState.addOrUpdateItem(state);

        if (!isHiddenPeer) {
            dialogsActor.send(new DialogsActor.PeerReadChanged(peer, date));
        }
    }

    @Verified
    private void onMessageReceived(long date) {
        if (date <= state.getOutReceiveDate()) {
            return;
        }
        state = state.changeOutReceiveDate(date);
        conversationState.addOrUpdateItem(state);

        if (!isHiddenPeer) {
            dialogsActor.send(new DialogsActor.PeerReceiveChanged(peer, date));
        }
    }

    @Verified
    private void onMessageReadByMe(long date) {
        if (date < state.getInReadDate()) {
            return;
        }
        state = state
                .changeInReadDate(date)
                .changeInMaxDate(Math.max(state.getInMaxMessageDate(), date));
        conversationState.addOrUpdateItem(state);

        inPendingIndex.removeBeforeValue(date);

        if (!isHiddenPeer) {
            dialogsActor.send(new DialogsActor.CounterChanged(peer, inPendingIndex.getCount()));
        }
        if (dialogsGroupedActor != null) {
            dialogsGroupedActor.send(new ActiveDialogsActor.CounterChanged(peer, inPendingIndex.getCount()));
        }
    }

    private void checkReadState(boolean updateDialogs) {
        if (state.getInMaxMessageDate() > state.getInReadDate()) {
            boolean inReadStateWasNull = state.getInReadDate() == 0;
            state = state.changeInReadDate(state.getInMaxMessageDate());
            conversationState.addOrUpdateItem(state);

            boolean wasNotNull = inPendingIndex.getCount() != 0;
            inPendingIndex.clear();
            readerActor.send(new OwnReadActor.MessageRead(peer, state.getInReadDate()));
            if ((wasNotNull || inReadStateWasNull) && updateDialogs) {
                if (!isHiddenPeer) {
                    dialogsActor.send(new DialogsActor.CounterChanged(peer, 0));
                }
                if (dialogsGroupedActor != null) {
                    dialogsGroupedActor.send(new ActiveDialogsActor.CounterChanged(peer, 0));
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

        inPendingIndex.remove(rids);

        // Updating dialog
        if (!isHiddenPeer) {
            dialogsActor.send(new DialogsActor.MessageDeleted(peer, messages.getHeadValue()));
        }
    }

    @Verified
    private void onClearConversation() {
        messages.clear();
        inPendingIndex.clear();
        dialogsActor.send(new DialogsActor.ChatClear(peer));
        state = state
                .changeInReadDate(0)
                .changeInMaxDate(0);
        conversationState.addOrUpdateItem(state);
    }

    @Verified
    private void onDeleteConversation() {
        messages.clear();
        inPendingIndex.clear();
        dialogsActor.send(new DialogsActor.ChatDelete(peer));
        state = state
                .changeInReadDate(0)
                .changeInMaxDate(0);
        conversationState.addOrUpdateItem(state);
    }

    // History

    @Verified
    private void onHistoryLoaded(List<Message> history, long maxReadDate, long maxReceiveDate) {

        boolean isChanged = false;
        if (state.getOutReceiveDate() < maxReceiveDate) {
            state = state.changeOutReceiveDate(maxReceiveDate);
            isChanged = true;
        }
        if (state.getOutReadDate() < maxReadDate) {
            state = state.changeOutReadDate(maxReadDate);
            isChanged = true;
        }
        if (isChanged) {
            conversationState.addOrUpdateItem(state);
        }

        ArrayList<Message> updated = new ArrayList<>();

        long maxReadMessage = 0;

        // Processing all new messages
        for (Message historyMessage : history) {
            // Ignore already present messages
            if (messages.getValue(historyMessage.getEngineId()) != null) {
                continue;
            }

            updated.add(historyMessage);

            if (historyMessage.getSenderId() != myUid()) {
                maxReadMessage = Math.max(maxReadMessage, historyMessage.getSortDate());
            }
        }

        // Updating messages
        if (updated.size() > 0) {
            messages.addOrUpdateItems(updated);
        }

        state = state.changeInMaxDate(Math.max(state.getInMaxMessageDate(), maxReadMessage));
        conversationState.addOrUpdateItem(state);

        if (isConversationAutoRead()) {
            checkReadState(true);
        }
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
            HistoryLoaded historyLoaded = ((HistoryLoaded) message);
            onHistoryLoaded(historyLoaded.getMessages(), historyLoaded.getMaxReadDate(),
                    historyLoaded.getMaxReceiveDate());
        } else if (message instanceof ClearConversation) {
            onClearConversation();
        } else if (message instanceof DeleteConversation) {
            onDeleteConversation();
        } else if (message instanceof MessagesDeleted) {
            onMessagesDeleted(((MessagesDeleted) message).getRids());
        } else if (message instanceof MessageReadByMe) {
            onMessageReadByMe(((MessageReadByMe) message).getDate());
        } else if (message instanceof MessageReactionsChanged) {
            onMessageReactionsUpdated(((MessageReactionsChanged) message).getRid(), ((MessageReactionsChanged) message).getReactions());
        } else if (message instanceof ConversationVisible) {
            onConversationVisible();
        } else if (message instanceof ConversationHidden) {
            onConversationHidden();
        } else if (message instanceof ConversationLoaded) {
            onConversationLoaded();
        } else {
            drop(message);
        }
    }

    @Override
    public void onBusEvent(Event event) {
        if (event instanceof AppVisibleChanged) {
            if (((AppVisibleChanged) event).isVisible()) {
                onAppVisible();
            } else {
                onAppHidden();
            }
        }
        super.onBusEvent(event);
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
        private long maxReadDate;
        private long maxReceiveDate;

        public HistoryLoaded(List<Message> messages, long maxReadDate, long maxReceiveDate) {
            this.messages = messages;
            this.maxReadDate = maxReadDate;
            this.maxReceiveDate = maxReceiveDate;
        }

        public List<Message> getMessages() {
            return messages;
        }

        public long getMaxReadDate() {
            return maxReadDate;
        }

        public long getMaxReceiveDate() {
            return maxReceiveDate;
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

    public static class MessageReactionsChanged {

        private long rid;
        private ArrayList<Reaction> reactions;

        public MessageReactionsChanged(long rid, ArrayList<Reaction> reactions) {
            this.rid = rid;
            this.reactions = reactions;
        }

        public ArrayList<Reaction> getReactions() {
            return reactions;
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

    public static class ConversationVisible {

    }

    public static class ConversationHidden {

    }

    public static class ConversationLoaded {

    }
}