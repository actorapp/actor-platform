package im.actor.model.modules.updates;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.Messenger;
import im.actor.model.api.rpc.ResponseLoadDialogs;
import im.actor.model.entity.*;
import im.actor.model.entity.MessageState;
import im.actor.model.entity.Peer;
import im.actor.model.entity.content.AbsContent;
import im.actor.model.entity.content.TextContent;
import im.actor.model.modules.entity.DialogHistory;
import im.actor.model.modules.entity.EntityConverter;
import im.actor.model.modules.messages.ConversationActor;
import im.actor.model.modules.messages.DialogsActor;
import im.actor.model.modules.messages.DialogsHistoryActor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class MessagesProcessor {
    private Messenger messenger;


    public MessagesProcessor(Messenger messenger) {
        this.messenger = messenger;

    }

    private long buildSortKey() {
        return System.currentTimeMillis();
    }

    public void onDialogsLoaded(ResponseLoadDialogs dialogsResponse) {
        ArrayList<DialogHistory> dialogs = new ArrayList<DialogHistory>();

        long maxLoadedDate = 0;

        for (im.actor.model.api.Dialog dialog : dialogsResponse.getDialogs()) {

            maxLoadedDate = Math.max(dialog.getSortDate(), maxLoadedDate);

            Peer peer = EntityConverter.convert(dialog.getPeer());
            AbsContent msgContent = convertContent(dialog.getMessage());

            if (msgContent == null) {
                continue;
            }

            dialogs.add(new DialogHistory(peer, dialog.getUnreadCount(), dialog.getSortDate(),
                    dialog.getRid(), dialog.getDate(), dialog.getSenderUid(), msgContent, convert(dialog.getState())));
        }

        messenger.getMessagesModule().getDialogsActor().send(new DialogsActor.HistoryLoaded(dialogs));
        messenger.getMessagesModule().getDialogsHistoryActor().send(new DialogsHistoryActor.LoadedMore(maxLoadedDate == 0, maxLoadedDate));
    }

    public void onMessage(im.actor.model.api.Peer _peer, int senderUid, long date, long rid,
                          im.actor.model.api.MessageContent content) {
        Peer peer = EntityConverter.convert(_peer);
        AbsContent msgContent = convertContent(content);

        if (msgContent == null) {
            return;
        }

        Message message = new Message(rid, date, date, senderUid,
                messenger.myUid() == senderUid ? MessageState.SENT : MessageState.UNKNOWN, msgContent);
        messenger.getMessagesModule().getConversationActor(peer).send(message);
    }

    public void onMessageRead(im.actor.model.api.Peer _peer, long startDate, long readDate) {
        Peer peer = EntityConverter.convert(_peer);
        messenger.getMessagesModule().getConversationActor(peer)
                .send(new ConversationActor.MessageRead(startDate));
    }

    public void onMessageEncryptedRead(im.actor.model.api.Peer _peer, long rid, long readDate) {
        Peer peer = EntityConverter.convert(_peer);
        // TODO: Implement
    }

    public void onMessageReceived(im.actor.model.api.Peer _peer, long startDate, long receivedDate) {
        Peer peer = EntityConverter.convert(_peer);
        messenger.getMessagesModule().getConversationActor(peer)
                .send(new ConversationActor.MessageReceived(startDate));
    }

    public void onMessageEncryptedReceived(im.actor.model.api.Peer _peer, long rid, long receivedDate) {
        Peer peer = EntityConverter.convert(_peer);
        // TODO: Implement
    }

    public void onMessageReadByMe(im.actor.model.api.Peer _peer, long startDate) {
        Peer peer = EntityConverter.convert(_peer);
        // TODO: Implement
    }

    public void onMessageEncryptedReadByMe(im.actor.model.api.Peer _peer, long rid) {
        Peer peer = EntityConverter.convert(_peer);
        // TODO: Implement
    }

    public void onMessageDelete(im.actor.model.api.Peer _peer, List<Long> rids) {
        Peer peer = EntityConverter.convert(_peer);
        messenger.getMessagesModule().getConversationActor(peer)
                .send(new ConversationActor.MessageDeleted(rids));
    }

    public void onMessageSent(im.actor.model.api.Peer _peer, long rid, long date) {
        Peer peer = EntityConverter.convert(_peer);
        messenger.getMessagesModule().getConversationActor(peer)
                .send(new ConversationActor.MessageSent(rid, date));
    }

    public void onChatClear(im.actor.model.api.Peer _peer) {
        Peer peer = EntityConverter.convert(_peer);
        // TODO: Move to conversation
        messenger.getMessagesModule().getDialogsActor().send(new DialogsActor.ChatClear(peer));
    }

    public void onChatDelete(im.actor.model.api.Peer _peer) {
        Peer peer = EntityConverter.convert(_peer);
        // TODO: Move to conversation
        messenger.getMessagesModule().getDialogsActor().send(new DialogsActor.ChatDelete(peer));
    }

    public void onUserRegistered(int uid) {
        // TODO: Implemented
    }

    private AbsContent convertContent(im.actor.model.api.MessageContent content) {
        if (content.getType() == 0x01) {
            try {
                im.actor.model.api.TextMessage textMessage = Bser.parse(new im.actor.model.api.TextMessage(),
                        content.getContent());
                return new TextContent(textMessage.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private MessageState convert(im.actor.model.api.MessageState state) {
        if (state == null) {
            return null;
        }
        switch (state) {
            case READ:
                return MessageState.READ;
            case RECEIVED:
                return MessageState.RECEIVED;
            case SENT:
                return MessageState.SENT;
        }

        return null;
    }
}