/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.messages;

import im.actor.core.entity.ContentDescription;
import im.actor.core.entity.Message;
import im.actor.core.entity.PeerEntity;
import im.actor.core.entity.content.TextContent;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.utils.ModuleActor;

public class OwnReadActor extends ModuleActor {

    public OwnReadActor(ModuleContext context) {
        super(context);
    }

    public void onInMessage(PeerEntity peer, Message message) {
        // Detecting if message already read
        long readState = context().getMessagesModule().loadReadState(peer);
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
        context().getNotificationsModule().onInMessage(peer, message.getSenderId(),
                message.getSortDate(), ContentDescription.fromContent(message.getContent()),
                hasUserMention);
    }

    public void onMessageRead(PeerEntity peer, long sortingDate) {
        // Detecting if message already read
        long readState = context().getMessagesModule().loadReadState(peer);
        if (sortingDate <= readState) {
            // Already read
            return;
        }

        // Mark as Read
        context().getMessagesModule().getPlainReadActor()
                .send(new CursorReaderActor.MarkRead(peer, sortingDate));

        // Update Counters
        context().getMessagesModule().getConversationActor(peer).send(new ConversationActor.MessageReadByMe(sortingDate));

        // Saving last read message
        context().getMessagesModule().saveReadState(peer, sortingDate);

        // Clearing notifications
        context().getNotificationsModule().onOwnRead(peer, sortingDate);
    }

    public void onMessageReadByMe(PeerEntity peer, long sortingDate) {
        long readState = context().getMessagesModule().loadReadState(peer);
        if (sortingDate <= readState) {
            // Already read
            return;
        }

        // Update Counters
        context().getMessagesModule().getConversationActor(peer).send(new ConversationActor.MessageReadByMe(sortingDate));

        // Saving read state
        context().getMessagesModule().saveReadState(peer, sortingDate);
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
        PeerEntity peer;
        long sortDate;

        public MessageReadByMe(PeerEntity peer, long sortDate) {
            this.peer = peer;
            this.sortDate = sortDate;
        }

        public PeerEntity getPeer() {
            return peer;
        }

        public long getSortDate() {
            return sortDate;
        }
    }

    public static class MessageRead {
        PeerEntity peer;
        long sortingDate;

        public MessageRead(PeerEntity peer, long sortingDate) {
            this.peer = peer;
            this.sortingDate = sortingDate;
        }

        public PeerEntity getPeer() {
            return peer;
        }

        public long getSortingDate() {
            return sortingDate;
        }
    }

    public static class InMessage {
        private PeerEntity peer;
        private Message message;

        public InMessage(PeerEntity peer, Message message) {
            this.peer = peer;
            this.message = message;
        }

        public PeerEntity getPeer() {
            return peer;
        }

        public Message getMessage() {
            return message;
        }
    }
}
