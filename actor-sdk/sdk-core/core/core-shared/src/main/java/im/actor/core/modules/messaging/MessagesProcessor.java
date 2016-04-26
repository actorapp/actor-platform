/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.messaging;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiDialogGroup;
import im.actor.core.api.ApiMessage;
import im.actor.core.api.ApiMessageReaction;
import im.actor.core.api.ApiPeer;
import im.actor.core.api.updates.UpdateChatClear;
import im.actor.core.api.updates.UpdateChatDelete;
import im.actor.core.api.updates.UpdateChatGroupsChanged;
import im.actor.core.api.updates.UpdateMessage;
import im.actor.core.api.updates.UpdateMessageContentChanged;
import im.actor.core.api.updates.UpdateMessageDelete;
import im.actor.core.api.updates.UpdateMessageRead;
import im.actor.core.api.updates.UpdateMessageReadByMe;
import im.actor.core.api.updates.UpdateMessageReceived;
import im.actor.core.api.updates.UpdateMessageSent;
import im.actor.core.api.updates.UpdateReactionsUpdate;
import im.actor.core.entity.Message;
import im.actor.core.entity.MessageState;
import im.actor.core.entity.Peer;
import im.actor.core.entity.Reaction;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.messaging.actions.SenderActor;
import im.actor.core.modules.sequence.processor.SequenceProcessor;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.annotations.Verified;
import im.actor.runtime.promise.Promise;

import static im.actor.core.entity.EntityConverter.convert;

public class MessagesProcessor extends AbsModule implements SequenceProcessor {

    public MessagesProcessor(ModuleContext context) {
        super(context);
    }

    public Promise<Void> onDifferenceStart() {
        context().getMessagesModule().getRouter().onDifferenceStart();
        return Promise.success(null);
    }

    public Promise<Void> onDifferenceEnd() {
        context().getMessagesModule().getRouter().onDifferenceEnd();
        return Promise.success(null);
    }

    @Verified
    public Promise<Void> onMessages(ApiPeer _peer, List<UpdateMessage> messages) {

        Peer peer = convert(_peer);

        ArrayList<Message> nMessages = new ArrayList<>();
        for (UpdateMessage u : messages) {

            AbsContent msgContent = AbsContent.fromMessage(u.getMessage());

            nMessages.add(new Message(
                    u.getRid(),
                    u.getDate(),
                    u.getDate(),
                    u.getSenderUid(),
                    myUid() == u.getSenderUid() ? MessageState.SENT : MessageState.UNKNOWN,
                    msgContent));
        }


        context().getMessagesModule().getRouter().onNewMessages(peer, nMessages);
        return Promise.success(null);
    }

    @Verified
    public Promise<Void> onMessage(ApiPeer _peer, int senderUid, long date, long rid, ApiMessage content) {

        Peer peer = convert(_peer);

        AbsContent msgContent = AbsContent.fromMessage(content);

        Message message = new Message(
                rid,
                date,
                date,
                senderUid,
                myUid() == senderUid ? MessageState.SENT : MessageState.UNKNOWN,
                msgContent);

        ArrayList<Message> messages = new ArrayList<>();
        messages.add(message);
        context().getMessagesModule().getRouter().onNewMessages(peer, messages);
        return Promise.success(null);
    }

    @Verified
    public Promise<Void> onMessageSent(ApiPeer _peer, long rid, long date) {
        Peer peer = convert(_peer);

        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)) {
            return Promise.success(null);
        }

        // Change message state in conversation
        context().getMessagesModule().getRouter().onOutgoingSent(peer, rid, date);

        // Notify Sender Actor
        sendActor().send(new SenderActor.MessageSent(peer, rid));
        return Promise.success(null);
    }

    @Verified
    public Promise<Void> onReactionsChanged(ApiPeer _peer, long rid, List<ApiMessageReaction> apiReactions) {
        Peer peer = convert(_peer);

        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)) {
            return Promise.success(null);
        }

        ArrayList<Reaction> reactions = new ArrayList<>();
        for (ApiMessageReaction r : apiReactions) {
            reactions.add(new Reaction(r.getCode(), r.getUsers()));
        }

        // Change message state in conversation
        context().getMessagesModule().getRouter().onReactionsChanged(peer, rid, reactions);
        return Promise.success(null);
    }

    @Verified
    public Promise<Void> onMessageContentChanged(ApiPeer _peer, long rid, ApiMessage message) {
        Peer peer = convert(_peer);
        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)) {
            return Promise.success(null);
        }
        AbsContent content = AbsContent.fromMessage(message);
        context().getMessagesModule().getRouter().onContentChanged(peer, rid, content);
        return Promise.success(null);
    }

    @Verified
    public Promise<Void> onMessageRead(ApiPeer _peer, long startDate) {
        Peer peer = convert(_peer);

        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)) {
            return Promise.success(null);
        }

        context().getMessagesModule().getRouter().onMessageRead(peer, startDate);

        return Promise.success(null);
    }

    @Verified
    public Promise<Void> onMessageReceived(ApiPeer _peer, long startDate) {
        Peer peer = convert(_peer);

        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)) {
            return Promise.success(null);
        }

        context().getMessagesModule().getRouter().onMessageReceived(peer, startDate);

        return Promise.success(null);
    }

    @Verified
    public Promise<Void> onMessageReadByMe(ApiPeer _peer, long startDate, int counter) {
        Peer peer = convert(_peer);

        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)) {
            return Promise.success(null);
        }

        context().getMessagesModule().getRouter().onMessageReadByMe(peer, startDate, counter);

        return Promise.success(null);
    }

    @Verified
    public Promise<Void> onMessageDelete(ApiPeer _peer, List<Long> rids) {
        Peer peer = convert(_peer);

        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)) {
            return Promise.success(null);
        }

        // Deleting messages from conversation
        context().getMessagesModule().getRouter().onMessagesDeleted(peer, rids);

        // TODO: Notify send actor

        return Promise.success(null);
    }

    @Verified
    public Promise<Void> onChatClear(ApiPeer _peer) {
        Peer peer = convert(_peer);

        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)) {
            return Promise.success(null);
        }

        context().getMessagesModule().getRouter().onChatClear(peer);

        // TODO: Notify send actor

        return Promise.success(null);
    }

    @Verified
    public Promise<Void> onChatDelete(ApiPeer _peer) {
        Peer peer = convert(_peer);

        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)) {
            return Promise.success(null);
        }

        context().getMessagesModule().getRouter().onChatDelete(peer);

        // TODO: Notify send actor

        return Promise.success(null);
    }

    @Verified
    public Promise<Void> onChatGroupsChanged(List<ApiDialogGroup> groups) {

        // TODO: Implement

        context().getMessagesModule().getRouter().onActiveDialogsChanged(groups, true, true);

        return Promise.success(null);
    }

    @Override
    public Promise<Void> process(Update update) {
        if (update instanceof UpdateMessage) {
            UpdateMessage message = (UpdateMessage) update;
            return onMessage(message.getPeer(), message.getSenderUid(), message.getDate(), message.getRid(),
                    message.getMessage());
        } else if (update instanceof UpdateMessageRead) {
            UpdateMessageRead messageRead = (UpdateMessageRead) update;
            return onMessageRead(messageRead.getPeer(), messageRead.getStartDate());
        } else if (update instanceof UpdateMessageReadByMe) {
            UpdateMessageReadByMe messageReadByMe = (UpdateMessageReadByMe) update;
            if (messageReadByMe.getUnreadCounter() != null) {
                return onMessageReadByMe(messageReadByMe.getPeer(), messageReadByMe.getStartDate(), messageReadByMe.getUnreadCounter());
            } else {
                return onMessageReadByMe(messageReadByMe.getPeer(), messageReadByMe.getStartDate(), 0);
            }
        } else if (update instanceof UpdateMessageReceived) {
            UpdateMessageReceived received = (UpdateMessageReceived) update;
            return onMessageReceived(received.getPeer(), received.getStartDate());
        } else if (update instanceof UpdateMessageDelete) {
            UpdateMessageDelete messageDelete = (UpdateMessageDelete) update;
            return onMessageDelete(messageDelete.getPeer(), messageDelete.getRids());
        } else if (update instanceof UpdateMessageSent) {
            UpdateMessageSent messageSent = (UpdateMessageSent) update;
            return onMessageSent(messageSent.getPeer(), messageSent.getRid(), messageSent.getDate());
        } else if (update instanceof UpdateMessageContentChanged) {
            UpdateMessageContentChanged contentChanged = (UpdateMessageContentChanged) update;
            return onMessageContentChanged(contentChanged.getPeer(), contentChanged.getRid(), contentChanged.getMessage());
        } else if (update instanceof UpdateChatClear) {
            UpdateChatClear chatClear = (UpdateChatClear) update;
            return onChatClear(chatClear.getPeer());
        } else if (update instanceof UpdateChatDelete) {
            UpdateChatDelete chatDelete = (UpdateChatDelete) update;
            return onChatDelete(chatDelete.getPeer());
        } else if (update instanceof UpdateChatGroupsChanged) {
            UpdateChatGroupsChanged chatGroupsChanged = (UpdateChatGroupsChanged) update;
            return onChatGroupsChanged(chatGroupsChanged.getDialogs());
        } else if (update instanceof UpdateReactionsUpdate) {
            return onReactionsChanged(((UpdateReactionsUpdate) update).getPeer(),
                    ((UpdateReactionsUpdate) update).getRid(), ((UpdateReactionsUpdate) update).getReactions());
        }
        return null;
    }
}
