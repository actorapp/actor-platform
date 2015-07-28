/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.messages;

import im.actor.model.entity.ContentDescription;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.content.TextContent;
import im.actor.model.modules.Modules;
import im.actor.model.modules.utils.ModuleActor;

public class OwnReadActor extends ModuleActor {

    public OwnReadActor(Modules messenger) {
        super(messenger);
    }

    public void onInMessage(Peer peer, Message message) {
        // Detecting if message already read
        long readState = modules().getMessagesModule().loadReadState(peer);
        if (message.getSortDate() <= readState) {
            // Already read
            return;
        }

        // Current mention?
        boolean hasUserMention = false;
        if (message.getContent() instanceof TextContent) {
            TextContent textContent = (TextContent) message.getContent();
            hasUserMention = textContent.getMentions().contains(myUid());
        }
        modules().getNotifications().onInMessage(peer, message.getSenderId(),
                message.getSortDate(), ContentDescription.fromContent(message.getContent()),
                hasUserMention);
    }

    public void onMessageRead(Peer peer, long sortingDate) {
        // Detecting if message already read
        long readState = modules().getMessagesModule().loadReadState(peer);
        if (sortingDate <= readState) {
            // Already read
            return;
        }

        // Mark as Read
        modules().getMessagesModule().getPlainReadActor()
                .send(new CursorReaderActor.MarkRead(peer, sortingDate));

        // Update Counters
        getConversationActor(peer).send(new ConversationActor.MessageReadByMe(sortingDate));

        // Saving last read message
        modules().getMessagesModule().saveReadState(peer, sortingDate);

        // Clearing notifications
        modules().getNotifications().onOwnRead(peer, sortingDate);
    }

    public void onMessageReadByMe(Peer peer, long sortingDate) {
        long readState = modules().getMessagesModule().loadReadState(peer);
        if (sortingDate <= readState) {
            // Already read
            return;
        }

        // Update Counters
        getConversationActor(peer).send(new ConversationActor.MessageReadByMe(sortingDate));

        // Saving read state
        modules().getMessagesModule().saveReadState(peer, sortingDate);
    }

    // Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof MessageRead) {
            MessageRead messageRead = (MessageRead) message;
            onMessageRead(messageRead.getPeer(), messageRead.getSortingDate());
        } else if (message instanceof MessageReadByMe) {
            MessageReadByMe readByMe = (MessageReadByMe) message;
            onMessageReadByMe(readByMe.getPeer(), readByMe.getSortDate());
        } else if (message instanceof InMessage) {
            InMessage inMessage = (InMessage) message;
            onInMessage(inMessage.getPeer(), inMessage.getMessage());
        } else {
            drop(message);
        }
    }

    public static class MessageReadByMe {
        Peer peer;
        long sortDate;

        public MessageReadByMe(Peer peer, long sortDate) {
            this.peer = peer;
            this.sortDate = sortDate;
        }

        public Peer getPeer() {
            return peer;
        }

        public long getSortDate() {
            return sortDate;
        }
    }

    public static class MessageRead {
        Peer peer;
        long sortingDate;

        public MessageRead(Peer peer, long sortingDate) {
            this.peer = peer;
            this.sortingDate = sortingDate;
        }

        public Peer getPeer() {
            return peer;
        }

        public long getSortingDate() {
            return sortingDate;
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
}
