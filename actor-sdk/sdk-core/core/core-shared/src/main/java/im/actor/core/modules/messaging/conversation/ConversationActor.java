/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.messaging.conversation;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.entity.ConversationState;
import im.actor.core.entity.Message;
import im.actor.core.entity.MessageState;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.Reaction;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.modules.ModuleContext;
import im.actor.core.events.AppVisibleChanged;
import im.actor.core.modules.ModuleActor;
import im.actor.core.modules.messaging.dialogs.ActiveDialogsActor;
import im.actor.core.modules.messaging.dialogs.DialogsActor;
import im.actor.core.modules.messaging.actions.OwnReadActor;
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

    private Peer peer;

    private ListEngine<Message> messages;

    public ConversationActor(Peer peer, ModuleContext context) {
        super(context);
        this.peer = peer;
    }

    @Override
    public void preStart() {
        messages = context().getMessagesModule().getConversationEngine(peer);
    }

    @Verified
    private void onInMessages(ArrayList<Message> inMessages) {
        messages.addOrUpdateItems(inMessages);
    }

    @Verified
    private void onInMessage(Message message) {
        messages.addOrUpdateItem(message);
    }

    @Verified
    private void onMessageContentUpdated(long rid, AbsContent content) {
        Message message = messages.getValue(rid);

        // Ignore if we already doesn't have this message
        if (message == null) {
            return;
        }

        messages.addOrUpdateItem(message.changeContent(content));
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
    }

    @Verified
    private void onClearConversation() {
        messages.clear();
    }

    @Verified
    private void onDeleteConversation() {
        messages.clear();
    }

    // History

    @Verified
    private void onHistoryLoaded(List<Message> history) {

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
        } else if (message instanceof HistoryLoaded) {
            HistoryLoaded historyLoaded = ((HistoryLoaded) message);
            onHistoryLoaded(historyLoaded.getMessages());
        } else if (message instanceof ClearConversation) {
            onClearConversation();
        } else if (message instanceof DeleteConversation) {
            onDeleteConversation();
        } else if (message instanceof MessagesDeleted) {
            onMessagesDeleted(((MessagesDeleted) message).getRids());
        } else if (message instanceof MessageReactionsChanged) {
            onMessageReactionsUpdated(((MessageReactionsChanged) message).getRid(), ((MessageReactionsChanged) message).getReactions());
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