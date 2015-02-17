package im.actor.model.modules.updates;

import im.actor.model.api.rpc.ResponseLoadDialogs;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.entity.*;
import im.actor.model.entity.MessageState;
import im.actor.model.entity.Peer;
import im.actor.model.entity.content.AbsContent;
import im.actor.model.entity.content.ServiceUserRegistered;
import im.actor.model.modules.BaseModule;
import im.actor.model.modules.Modules;
import im.actor.model.modules.messages.PlainReceiverActor;
import im.actor.model.modules.messages.SenderActor;
import im.actor.model.modules.messages.entity.DialogHistory;
import im.actor.model.modules.messages.ConversationActor;
import im.actor.model.modules.messages.DialogsActor;
import im.actor.model.modules.messages.DialogsHistoryActor;
import im.actor.model.modules.messages.OwnReadActor;
import im.actor.model.modules.utils.RandomUtils;

import java.util.ArrayList;
import java.util.List;

import static im.actor.model.modules.messages.entity.EntityConverter.convert;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class MessagesProcessor extends BaseModule {
    public MessagesProcessor(Modules messenger) {
        super(messenger);
    }

    private ActorRef sendActor() {
        return modules().getMessagesModule().getSendMessageActor();
    }

    private ActorRef dialogsActor() {
        return modules().getMessagesModule().getDialogsActor();
    }

    private ActorRef dialogsHistoryActor() {
        return modules().getMessagesModule().getDialogsHistoryActor();
    }

    private ActorRef ownReadActor() {
        return modules().getMessagesModule().getOwnReadActor();
    }

    private ActorRef plainReceiveActor() {
        return modules().getMessagesModule().getPlainReceiverActor();
    }

    private ActorRef conversationActor(Peer peer) {
        return modules().getMessagesModule().getConversationActor(peer);
    }

    public void onDialogsLoaded(ResponseLoadDialogs dialogsResponse) {
        ArrayList<DialogHistory> dialogs = new ArrayList<DialogHistory>();

        long maxLoadedDate = 0;

        for (im.actor.model.api.Dialog dialog : dialogsResponse.getDialogs()) {

            maxLoadedDate = Math.max(dialog.getSortDate(), maxLoadedDate);

            Peer peer = convert(dialog.getPeer());
            AbsContent msgContent = convert(dialog.getMessage());

            if (msgContent == null) {
                continue;
            }

            dialogs.add(new DialogHistory(peer, dialog.getUnreadCount(), dialog.getSortDate(),
                    dialog.getRid(), dialog.getDate(), dialog.getSenderUid(), msgContent, convert(dialog.getState())));
        }

        dialogsActor().send(new DialogsActor.HistoryLoaded(dialogs));
        dialogsHistoryActor().send(new DialogsHistoryActor.LoadedMore(maxLoadedDate == 0, maxLoadedDate));
    }

    public void onMessage(im.actor.model.api.Peer _peer, int senderUid, long date, long rid,
                          im.actor.model.api.MessageContent content) {
        Peer peer = convert(_peer);
        AbsContent msgContent = convert(content);

        if (msgContent == null) {
            return;
        }

        boolean isOut = myUid() == senderUid;

        Message message = new Message(rid, date, date, senderUid,
                isOut ? MessageState.SENT : MessageState.UNKNOWN, msgContent);
        conversationActor(peer).send(message);

        if (!isOut) {
            ownReadActor().send(new OwnReadActor.NewMessage(peer, rid, date, false));
            plainReceiveActor().send(new PlainReceiverActor.MarkReceived(peer, date));
        } else {
            ownReadActor().send(new OwnReadActor.NewOutMessage(peer, rid, date, false));
        }
    }

    public void onMessageRead(im.actor.model.api.Peer _peer, long startDate, long readDate) {
        Peer peer = convert(_peer);
        conversationActor(peer).send(new ConversationActor.MessageRead(startDate));
    }

    public void onMessageEncryptedRead(im.actor.model.api.Peer _peer, long rid, long readDate) {
        Peer peer = convert(_peer);
        conversationActor(peer).send(new ConversationActor.MessageEncryptedRead(rid));
    }

    public void onMessageReceived(im.actor.model.api.Peer _peer, long startDate, long receivedDate) {
        Peer peer = convert(_peer);
        conversationActor(peer).send(new ConversationActor.MessageReceived(startDate));
    }

    public void onMessageEncryptedReceived(im.actor.model.api.Peer _peer, long rid, long receivedDate) {
        Peer peer = convert(_peer);
        conversationActor(peer).send(new ConversationActor.MessageEncryptedReceived(rid));
    }

    public void onMessageReadByMe(im.actor.model.api.Peer _peer, long startDate) {
        Peer peer = convert(_peer);
        ownReadActor().send(new OwnReadActor.MessageReadByMe(peer, startDate));
    }

    public void onMessageEncryptedReadByMe(im.actor.model.api.Peer _peer, long rid) {
        Peer peer = convert(_peer);
        ownReadActor().send(new OwnReadActor.MessageReadByMeEncrypted(peer, rid));
    }

    public void onMessageDelete(im.actor.model.api.Peer _peer, List<Long> rids) {
        Peer peer = convert(_peer);
        conversationActor(peer).send(new ConversationActor.MessageDeleted(rids));

        // TODO: Notify own read actor
        // TODO: Notify send actor
    }

    public void onMessageSent(im.actor.model.api.Peer _peer, long rid, long date) {
        Peer peer = convert(_peer);
        conversationActor(peer).send(new ConversationActor.MessageSent(rid, date));
        sendActor().send(new SenderActor.MessageSent(peer, rid));

        // TODO: Notify own read actor
    }

    public void onChatClear(im.actor.model.api.Peer _peer) {
        Peer peer = convert(_peer);
        dialogsActor().send(new DialogsActor.ChatClear(peer));

        // TODO: Move to conversation
        // TODO: Notify own read actor
        // TODO: Notify send actor
    }

    public void onChatDelete(im.actor.model.api.Peer _peer) {
        Peer peer = convert(_peer);
        dialogsActor().send(new DialogsActor.ChatDelete(peer));

        // TODO: Move to conversation
        // TODO: Notify own read actor
        // TODO: Notify send actor
    }

    public void onUserRegistered(int uid, long date) {
        Message message = new Message(RandomUtils.nextRid(), date, date, uid,
                MessageState.UNKNOWN, new ServiceUserRegistered());
        conversationActor(Peer.user(uid)).send(message);

        // TODO: Notify own read actor
    }

}