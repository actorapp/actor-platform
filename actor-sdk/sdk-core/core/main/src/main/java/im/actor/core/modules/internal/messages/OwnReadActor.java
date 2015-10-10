/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.messages;

import im.actor.core.entity.ContentDescription;
import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.content.TextContent;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.utils.ModuleActor;

public class OwnReadActor extends ModuleActor {

    private boolean isInDifference = false;

    public OwnReadActor(ModuleContext context) {
        super(context);
    }

    public void onDifferenceStart() {
        if (isInDifference) {
            return;
        }
        isInDifference = true;

        context().getNotificationsModule().pauseNotifications();
    }

    public void onDifferenceEnd() {
        if (!isInDifference) {
            return;
        }
        isInDifference = false;

        context().getNotificationsModule().resumeNotifications();
    }

    public void onInMessage(Peer peer, Message message) {
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

    public void onOutMessage(Peer peer, long sortDate) {
        long readState = context().getMessagesModule().loadReadState(peer);
        if (sortDate <= readState) {
            // Already read
            return;
        }

        // Update Counters
        // context().getMessagesModule().getConversationActor(peer).send(new ConversationActor.MessageReadByMe(sortingDate));

        // Saving read state
        context().getMessagesModule().saveReadState(peer, sortDate);
    }

    public void onMessageRead(Peer peer, long sortingDate) {
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

    public void onMessageReadByMe(Peer peer, long sortingDate) {
        long readState = context().getMessagesModule().loadReadState(peer);
        if (sortingDate <= readState) {
            // Already read
            return;
        }

        // Update Counters
        context().getMessagesModule().getConversationActor(peer).send(new ConversationActor.MessageReadByMe(sortingDate));

        // Saving read state
        context().getMessagesModule().saveReadState(peer, sortingDate);

        // Clearing notifications
        context().getNotificationsModule().onOwnRead(peer, sortingDate);
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
        } else if (message instanceof OutMessage) {
            OutMessage outMessage = (OutMessage) message;
            onOutMessage(outMessage.getPeer(), outMessage.getSortDate());
        } else if (message instanceof StartGetDifference) {
            onDifferenceStart();
        } else if (message instanceof StopGetDifference) {
            onDifferenceEnd();
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

    public static class OutMessage {
        private Peer peer;
        private long sortDate;

        public OutMessage(Peer peer, long sortDate) {
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

    public static class StartGetDifference {

    }

    public static class StopGetDifference {

    }
}
