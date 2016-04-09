/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.messaging;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiDialogGroup;
import im.actor.core.api.ApiMessage;
import im.actor.core.api.ApiMessageContainer;
import im.actor.core.api.ApiMessageReaction;
import im.actor.core.api.ApiMessageState;
import im.actor.core.api.ApiPeer;
import im.actor.core.api.rpc.ResponseLoadArchived;
import im.actor.core.api.rpc.ResponseLoadHistory;
import im.actor.core.api.updates.UpdateMessage;
import im.actor.core.entity.Message;
import im.actor.core.entity.MessageState;
import im.actor.core.entity.Peer;
import im.actor.core.entity.Reaction;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.messaging.actions.SenderActor;
import im.actor.core.entity.EntityConverter;
import im.actor.runtime.annotations.Verified;

import static im.actor.core.entity.EntityConverter.convert;

public class MessagesProcessor extends AbsModule {

    public MessagesProcessor(ModuleContext context) {
        super(context);
    }

    @Verified
    public void onMessages(ApiPeer _peer, List<UpdateMessage> messages) {

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
    }

    @Verified
    public void onMessage(ApiPeer _peer, int senderUid, long date, long rid, ApiMessage content) {

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
    }

    @Verified
    public void onMessageSent(ApiPeer _peer, long rid, long date) {
        Peer peer = convert(_peer);

        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)) {
            return;
        }

        // Change message state in conversation
        context().getMessagesModule().getRouter().onOutgoingSent(peer, rid, date);

        // Notify Sender Actor
        sendActor().send(new SenderActor.MessageSent(peer, rid));
    }

    @Verified
    public void onReactionsChanged(ApiPeer _peer, long rid, List<ApiMessageReaction> apiReactions) {
        Peer peer = convert(_peer);

        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)) {
            return;
        }

        ArrayList<Reaction> reactions = new ArrayList<>();
        for (ApiMessageReaction r : apiReactions) {
            reactions.add(new Reaction(r.getCode(), r.getUsers()));
        }

        // Change message state in conversation
        context().getMessagesModule().getRouter().onReactionsChanged(peer, rid, reactions);
    }

    @Verified
    public void onMessageContentChanged(ApiPeer _peer, long rid,
                                        ApiMessage message) {
        Peer peer = convert(_peer);
        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)) {
            return;
        }
        AbsContent content = AbsContent.fromMessage(message);
        context().getMessagesModule().getRouter().onContentChanged(peer, rid, content);
    }

    @Verified
    public void onMessageRead(ApiPeer _peer, long startDate) {
        Peer peer = convert(_peer);

        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)) {
            return;
        }

        context().getMessagesModule().getRouter().onMessageRead(peer, startDate);
    }

    @Verified
    public void onMessageReceived(ApiPeer _peer, long startDate) {
        Peer peer = convert(_peer);

        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)) {
            return;
        }

        context().getMessagesModule().getRouter().onMessageReceived(peer, startDate);
    }

    @Verified
    public void onMessageReadByMe(ApiPeer _peer, long startDate, int counter) {
        Peer peer = convert(_peer);

        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)) {
            return;
        }

        context().getMessagesModule().getRouter().onMessageReadByMe(peer, startDate, counter);
    }


    @Verified
    public void onMessageDelete(ApiPeer _peer, List<Long> rids) {
        Peer peer = convert(_peer);

        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)) {
            return;
        }

        // Deleting messages from conversation
        context().getMessagesModule().getRouter().onMessagesDeleted(peer, rids);

        // TODO: Notify send actor
    }

    @Verified
    public void onChatClear(ApiPeer _peer) {
//        Peer peer = convert(_peer);
//
//        // We are not invalidating sequence because of this update
//        if (!isValidPeer(peer)) {
//            return;
//        }
//
//        // Clearing conversation
//        conversationActor(peer).send(new ConversationActor.ClearConversation());
//
//        // TODO: Notify send actor
    }

    @Verified
    public void onChatDelete(ApiPeer _peer) {
//        Peer peer = convert(_peer);
//
//        // We are not invalidating sequence because of this update
//        if (!isValidPeer(peer)) {
//            return;
//        }
//
//        // Deleting conversation
//        conversationActor(peer).send(new ConversationActor.DeleteConversation());
//
//        // TODO: Notify send actor
    }

    @Verified
    public void onMessagesLoaded(Peer peer, ResponseLoadHistory historyResponse) {
        ArrayList<Message> messages = new ArrayList<Message>();
        long maxLoadedDate = Long.MAX_VALUE;
        long maxReadDate = 0;
        long maxReceiveDate = 0;
        for (ApiMessageContainer historyMessage : historyResponse.getHistory()) {

            maxLoadedDate = Math.min(historyMessage.getDate(), maxLoadedDate);

            AbsContent content = AbsContent.fromMessage(historyMessage.getMessage());

            MessageState state = EntityConverter.convert(historyMessage.getState());

            if (historyMessage.getState() == ApiMessageState.RECEIVED) {
                maxReceiveDate = Math.max(historyMessage.getDate(), maxReceiveDate);
            } else if (historyMessage.getState() == ApiMessageState.READ) {
                maxReceiveDate = Math.max(historyMessage.getDate(), maxReceiveDate);
                maxReadDate = Math.max(historyMessage.getDate(), maxReadDate);
            }

            ArrayList<Reaction> reactions = new ArrayList<Reaction>();

            for (ApiMessageReaction r : historyMessage.getReactions()) {
                reactions.add(new Reaction(r.getCode(), r.getUsers()));
            }

            messages.add(new Message(historyMessage.getRid(), historyMessage.getDate(),
                    historyMessage.getDate(), historyMessage.getSenderUid(),
                    state, content, reactions, 0));
        }

//        // Sending updates to conversation actor
//        if (messages.size() > 0) {
//            conversationActor(peer).send(new ConversationActor.HistoryLoaded(messages));
//        }

        // Sending notification to conversation history actor
//        conversationHistoryActor(peer).send(new ConversationHistoryActor.LoadedMore(historyResponse.getHistory().size(),
//                maxLoadedDate));
    }

    public void onChatGroupsChanged(List<ApiDialogGroup> groups) {
//        if (context().getConfiguration().isEnabledGroupedChatList()) {
//            context().getMessagesModule().getDialogsGroupedActor()
//                    .send(new ActiveDialogsActor.GroupedDialogsChanged(groups));
//        }
    }

    public void onArchivedDialogsLoaded(ResponseLoadArchived responseLoadArchived) {
//        archivedDialogsActor().send(new ArchivedDialogsActor.LoadedMore(responseLoadArchived));
    }



    @Verified
    public void onUserRegistered(long rid, int uid, long date) {
//        Message message = new Message(rid, date, date, uid,
//                MessageState.UNKNOWN, ServiceUserRegistered.create(), new ArrayList<Reaction>());
//
//        conversationActor(Peer.user(uid)).send(message);
    }
}
