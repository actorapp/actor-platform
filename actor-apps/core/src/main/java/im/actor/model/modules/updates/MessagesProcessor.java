/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.updates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.model.annotation.Verified;
import im.actor.model.api.HistoryMessage;
import im.actor.model.api.rpc.ResponseLoadDialogs;
import im.actor.model.api.rpc.ResponseLoadHistory;
import im.actor.model.entity.Message;
import im.actor.model.entity.MessageState;
import im.actor.model.entity.Peer;
import im.actor.model.entity.content.AbsContent;
import im.actor.model.entity.content.ServiceUserRegistered;
import im.actor.model.entity.content.TextContent;
import im.actor.model.modules.BaseModule;
import im.actor.model.modules.Modules;
import im.actor.model.modules.messages.ConversationActor;
import im.actor.model.modules.messages.ConversationHistoryActor;
import im.actor.model.modules.messages.CursorReceiverActor;
import im.actor.model.modules.messages.DialogsActor;
import im.actor.model.modules.messages.DialogsHistoryActor;
import im.actor.model.modules.messages.SenderActor;
import im.actor.model.modules.messages.entity.DialogHistory;
import im.actor.model.modules.messages.entity.EntityConverter;

import static im.actor.model.modules.messages.entity.EntityConverter.convert;

public class MessagesProcessor extends BaseModule {
    public MessagesProcessor(Modules messenger) {
        super(messenger);
    }

    @Verified
    public void onDialogsLoaded(ResponseLoadDialogs dialogsResponse) {

        // Should we eliminate DialogHistory?

        ArrayList<DialogHistory> dialogs = new ArrayList<DialogHistory>();

        long maxLoadedDate = Long.MAX_VALUE;

        for (im.actor.model.api.Dialog dialog : dialogsResponse.getDialogs()) {

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
            modules().getAppStateModule().onDialogsLoaded();
        }

        // Sending notification to history actor
        dialogsHistoryActor().send(new DialogsHistoryActor.LoadedMore(dialogsResponse.getDialogs().size(),
                maxLoadedDate));
    }

    @Verified
    public void onMessagesLoaded(Peer peer, ResponseLoadHistory historyResponse) {
        ArrayList<Message> messages = new ArrayList<Message>();
        long maxLoadedDate = Long.MAX_VALUE;
        for (HistoryMessage historyMessage : historyResponse.getHistory()) {

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


    public void onMessage(im.actor.model.api.Peer _peer, int senderUid, long date, long rid,
                          im.actor.model.api.Message content) {

        Peer peer = convert(_peer);
        AbsContent msgContent = null;
        try {
            msgContent = AbsContent.fromMessage(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (msgContent == null) {
            // Ignore if content is unsupported
            return;
        }

        onMessage(peer, senderUid, date, rid, msgContent);
    }

    private void onMessage(Peer peer, int senderUid, long date, long rid, AbsContent msgContent) {
        boolean isOut = myUid() == senderUid;

        // Sending message to conversation
        Message message = new Message(rid, date, date, senderUid,
                isOut ? MessageState.SENT : MessageState.UNKNOWN, msgContent);
        conversationActor(peer).send(message);

        if (!isOut) {

            // Send to OwnReadActor for adding to unread index
            boolean hasCurrentUserMention = false;
            AbsContent content = message.getContent();
            if (content instanceof TextContent) {
                ArrayList<Integer> mentions = ((TextContent) content).getMentions();
                hasCurrentUserMention = mentions != null && mentions.contains(myUid());
            }

            // TODO: Remove
            //ownReadActor().send(new OwnReadActor.NewMessage(peer, rid, date, senderUid,
            //        ContentDescription.fromContent(content), hasCurrentUserMention));

            // mark message as received
            plainReceiveActor().send(new CursorReceiverActor.MarkReceived(peer, date));

        } else {

            // TODO: Remove
            // Send information to OwnReadActor about out message
            // ownReadActor().send(new OwnReadActor.MessageRead(peer, date));
        }
    }

    @Verified
    public void onMessageRead(im.actor.model.api.Peer _peer, long startDate, long readDate) {
        Peer peer = convert(_peer);

        // Sending event to conversation actor
        conversationActor(peer).send(new ConversationActor.MessageRead(startDate));
    }

    @Verified
    public void onMessageReceived(im.actor.model.api.Peer _peer, long startDate, long receivedDate) {
        Peer peer = convert(_peer);

        // Sending event to conversation actor
        conversationActor(peer).send(new ConversationActor.MessageReceived(startDate));
    }

    @Verified
    public void onMessageReadByMe(im.actor.model.api.Peer _peer, long startDate) {
        Peer peer = convert(_peer);

        // TODO: Move to Conversation Actor
        // Sending event to OwnReadActor for syncing read state across devices
        // ownReadActor().send(new OwnReadActor.MessageReadByMe(peer, startDate));
    }

    public void onMessageDelete(im.actor.model.api.Peer _peer, List<Long> rids) {
        Peer peer = convert(_peer);

        // Deleting messages from conversation
        conversationActor(peer).send(new ConversationActor.MessagesDeleted(rids));

        // TODO: Remove
        // Remove messages from unread index
        // ownReadActor().send(new OwnReadActor.MessageDeleted(peer, rids));

        // TODO: Notify send actor for canceling
    }

    public void onMessageSent(im.actor.model.api.Peer _peer, long rid, long date) {
        Peer peer = convert(_peer);

        // Change message state in conversation
        conversationActor(peer).send(new ConversationActor.MessageSent(rid, date));

        // Notify Sender Actor
        sendActor().send(new SenderActor.MessageSent(peer, rid));

        // TODO: Remove
        // Send information to OwnReadActor about out message
        // ownReadActor().send(new OwnReadActor.MessageRead(peer, date));
    }

    public void onMessageDateChanged(im.actor.model.api.Peer _peer, long rid, long ndate) {
        Peer peer = convert(_peer);

        // Change message state in conversation
        conversationActor(peer).send(new ConversationActor.MessageDateChange(rid, ndate));
    }

    public void onMessageContentChanged(im.actor.model.api.Peer _peer, long rid,
                                        im.actor.model.api.Message message) {

        Peer peer = convert(_peer);

        // Change message state in conversation
        try {
            conversationActor(peer).send(new ConversationActor.MessageContentUpdated(rid, AbsContent.fromMessage(message)));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public void onChatClear(im.actor.model.api.Peer _peer) {
        Peer peer = convert(_peer);

        // TODO: Notify own read actor
        // TODO: Notify send actor

        // Clearing conversation
        conversationActor(peer).send(new ConversationActor.ClearConversation());
    }

    public void onChatDelete(im.actor.model.api.Peer _peer) {
        Peer peer = convert(_peer);

        // TODO: Notify own read actor
        // TODO: Notify send actor

        // Deleting conversation
        conversationActor(peer).send(new ConversationActor.DeleteConversation());
    }

    public void onUserRegistered(long rid, int uid, long date) {
        Message message = new Message(rid, date, date, uid,
                MessageState.UNKNOWN, ServiceUserRegistered.create());

        // TODO: Remove
        //ownReadActor().send(new OwnReadActor
        //        .NewMessage(new Peer(PeerType.PRIVATE, uid), rid, date));
        conversationActor(Peer.user(uid)).send(message);
    }
}