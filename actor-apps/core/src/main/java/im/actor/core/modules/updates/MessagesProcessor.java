/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.updates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiDialog;
import im.actor.core.api.ApiMessage;
import im.actor.core.api.ApiPeer;
import im.actor.core.api.ApiAppCounters;
import im.actor.core.api.ApiHistoryMessage;
import im.actor.core.api.rpc.ResponseLoadDialogs;
import im.actor.core.api.rpc.ResponseLoadHistory;
import im.actor.core.api.updates.UpdateMessage;
import im.actor.core.entity.Message;
import im.actor.core.entity.MessageState;
import im.actor.core.entity.Peer;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.entity.content.ServiceUserRegistered;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.messages.ConversationActor;
import im.actor.core.modules.internal.messages.ConversationHistoryActor;
import im.actor.core.modules.internal.messages.CursorReceiverActor;
import im.actor.core.modules.internal.messages.DialogsActor;
import im.actor.core.modules.internal.messages.DialogsHistoryActor;
import im.actor.core.modules.internal.messages.OwnReadActor;
import im.actor.core.modules.internal.messages.SenderActor;
import im.actor.core.modules.internal.messages.entity.DialogHistory;
import im.actor.core.modules.internal.messages.entity.EntityConverter;
import im.actor.runtime.annotations.Verified;

import static im.actor.core.modules.internal.messages.entity.EntityConverter.convert;

public class MessagesProcessor extends AbsModule {

    public MessagesProcessor(ModuleContext context) {
        super(context);
    }

    public void onMessages(ApiPeer _peer, List<UpdateMessage> messages) {

        long outMessageSortDate = 0;
        long intMessageSortDate = 0;
        Peer peer = convert(_peer);

        ArrayList<Message> nMesages = new ArrayList<Message>();
        for (UpdateMessage u : messages) {

            AbsContent msgContent;
            try {
                msgContent = AbsContent.fromMessage(u.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            boolean isOut = myUid() == u.getSenderUid();

            // Sending message to conversation
            nMesages.add(new Message(u.getRid(), u.getDate(), u.getDate(), u.getSenderUid(),
                    isOut ? MessageState.SENT : MessageState.UNKNOWN, msgContent));

            if (!isOut) {

                intMessageSortDate = Math.max(intMessageSortDate, u.getDate());
            } else {
                outMessageSortDate = Math.max(outMessageSortDate, u.getDate());
            }
        }

        conversationActor(peer).send(new ConversationActor.Messages(nMesages));

        if (intMessageSortDate > 0) {
            plainReceiveActor().send(new CursorReceiverActor.MarkReceived(peer, intMessageSortDate));
        }

        if (outMessageSortDate > 0) {
            ownReadActor().send(new OwnReadActor.OutMessage(peer, outMessageSortDate));
        }

        // OwnReadActor
        for (Message m : nMesages) {
            if (m.getSenderId() != myUid()) {
                ownReadActor().send(new OwnReadActor.InMessage(peer, m));
            }
        }
    }

    @Verified
    public void onMessage(ApiPeer _peer, int senderUid, long date, long rid,
                          ApiMessage content) {

        Peer peer = convert(_peer);
        AbsContent msgContent;
        try {
            msgContent = AbsContent.fromMessage(content);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        boolean isOut = myUid() == senderUid;

        // Sending message to conversation
        Message message = new Message(rid, date, date, senderUid,
                isOut ? MessageState.SENT : MessageState.UNKNOWN, msgContent);

        conversationActor(peer).send(message);

        if (!isOut) {
            // mark message as received
            plainReceiveActor().send(new CursorReceiverActor.MarkReceived(peer, date));

            // Send to own read actor
            ownReadActor().send(new OwnReadActor.InMessage(peer, message));
        } else {
            // Send to own read actor
            ownReadActor().send(new OwnReadActor.OutMessage(peer, message.getSortDate()));
        }
    }

    @Verified
    public void onUserRegistered(long rid, int uid, long date) {
        Message message = new Message(rid, date, date, uid,
                MessageState.UNKNOWN, ServiceUserRegistered.create());

        conversationActor(Peer.user(uid)).send(message);
    }

    @Verified
    public void onMessageRead(ApiPeer _peer, long startDate) {
        Peer peer = convert(_peer);

        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)){
            return;
        }

        // Sending event to conversation actor
        conversationActor(peer).send(new ConversationActor.MessageRead(startDate));
    }

    @Verified
    public void onMessageReceived(ApiPeer _peer, long startDate) {
        Peer peer = convert(_peer);

        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)){
            return;
        }

        // Sending event to conversation actor
        conversationActor(peer).send(new ConversationActor.MessageReceived(startDate));
    }

    @Verified
    public void onMessageReadByMe(ApiPeer _peer, long startDate) {
        Peer peer = convert(_peer);

        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)){
            return;
        }

        // Sending event to own read actor
        ownReadActor().send(new OwnReadActor.MessageReadByMe(peer, startDate));
    }

    @Verified
    public void onMessageSent(ApiPeer _peer, long rid, long date) {
        Peer peer = convert(_peer);

        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)){
            return;
        }

        // Change message state in conversation
        conversationActor(peer).send(new ConversationActor.MessageSent(rid, date));

        // Notify Sender Actor
        sendActor().send(new SenderActor.MessageSent(peer, rid));

        // Send to own read actor
        ownReadActor().send(new OwnReadActor.OutMessage(peer, date));
    }

    @Verified
    public void onMessageContentChanged(ApiPeer _peer, long rid,
                                        ApiMessage message) {
        Peer peer = convert(_peer);

        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)){
            return;
        }

        AbsContent content;
        try {
            content = AbsContent.fromMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Change message content in conversation
        conversationActor(peer).send(new ConversationActor.MessageContentUpdated(rid, content));
    }

    @Verified
    public void onMessageDelete(ApiPeer _peer, List<Long> rids) {
        Peer peer = convert(_peer);

        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)){
            return;
        }

        // Deleting messages from conversation
        conversationActor(peer).send(new ConversationActor.MessagesDeleted(rids));

        // TODO: Notify send actor
    }

    @Verified
    public void onChatClear(ApiPeer _peer) {
        Peer peer = convert(_peer);

        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)){
            return;
        }

        // Clearing conversation
        conversationActor(peer).send(new ConversationActor.ClearConversation());

        // TODO: Notify send actor
    }

    @Verified
    public void onChatDelete(ApiPeer _peer) {
        Peer peer = convert(_peer);

        // We are not invalidating sequence because of this update
        if (!isValidPeer(peer)){
            return;
        }

        // Deleting conversation
        conversationActor(peer).send(new ConversationActor.DeleteConversation());

        // TODO: Notify send actor
    }

    @Verified
    public void onDialogsLoaded(ResponseLoadDialogs dialogsResponse) {

        // Should we eliminate DialogHistory?

        ArrayList<DialogHistory> dialogs = new ArrayList<DialogHistory>();

        long maxLoadedDate = Long.MAX_VALUE;

        for (ApiDialog dialog : dialogsResponse.getDialogs()) {

            maxLoadedDate = Math.min(dialog.getSortDate(), maxLoadedDate);

            Peer peer = convert(dialog.getPeer());

            AbsContent msgContent = null;
            try {
                msgContent = AbsContent.fromMessage(dialog.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (msgContent == null) {
                continue;
            }

            dialogs.add(new DialogHistory(peer, dialog.getUnreadCount(), dialog.getSortDate(),
                    dialog.getRid(), dialog.getDate(), dialog.getSenderUid(), msgContent, convert(dialog.getState())));
        }

        // Sending updates to dialogs actor
        if (dialogs.size() > 0) {
            dialogsActor().send(new DialogsActor.HistoryLoaded(dialogs));
        } else {
            context().getAppStateModule().onDialogsLoaded();
        }

        // Sending notification to history actor
        dialogsHistoryActor().send(new DialogsHistoryActor.LoadedMore(dialogsResponse.getDialogs().size(),
                maxLoadedDate));
    }

    @Verified
    public void onMessagesLoaded(Peer peer, ResponseLoadHistory historyResponse) {
        ArrayList<Message> messages = new ArrayList<Message>();
        long maxLoadedDate = Long.MAX_VALUE;
        for (ApiHistoryMessage historyMessage : historyResponse.getHistory()) {

            maxLoadedDate = Math.min(historyMessage.getDate(), maxLoadedDate);

            AbsContent content = null;
            try {
                content = AbsContent.fromMessage(historyMessage.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (content == null) {
                continue;
            }
            MessageState state = EntityConverter.convert(historyMessage.getState());

            messages.add(new Message(historyMessage.getRid(), historyMessage.getDate(),
                    historyMessage.getDate(), historyMessage.getSenderUid(),
                    state, content));
        }

        // Sending updates to conversation actor
        if (messages.size() > 0) {
            conversationActor(peer).send(new ConversationActor.HistoryLoaded(messages));
        }

        // Sending notification to conversation history actor
        conversationHistoryActor(peer).send(new ConversationHistoryActor.LoadedMore(historyResponse.getHistory().size(),
                maxLoadedDate));
    }

    public void onCountersChanged(ApiAppCounters counters) {
        context().getAppStateModule().onCountersChanged(counters);
    }
}