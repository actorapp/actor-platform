package im.actor.model.modules.updates;

import com.droidkit.bser.Bser;
import im.actor.model.Messenger;
import im.actor.model.entity.EntityConverter;
import im.actor.model.entity.Message;
import im.actor.model.entity.MessageState;
import im.actor.model.entity.Peer;
import im.actor.model.entity.content.AbsContent;
import im.actor.model.entity.content.TextContent;
import im.actor.model.modules.messages.DialogsActor;

import java.io.IOException;
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

    public void onMessage(im.actor.model.api.Peer _peer, int senderUid, long date, long rid,
                          im.actor.model.api.MessageContent content) {
        Peer peer = EntityConverter.convert(_peer);
        AbsContent msgContent;
        if (content.getType() == 0x01) {
            try {
                im.actor.model.api.TextMessage textMessage = Bser.parse(new im.actor.model.api.TextMessage(),
                        content.getContent());
                msgContent = new TextContent(textMessage.getText());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
            return;
        }
        Message message = new Message(rid, buildSortKey(), date, senderUid,
                messenger.myUid() == senderUid ? MessageState.SENT : MessageState.UNKNOWN, msgContent);
        messenger.getMessagesModule().getConversationActor(peer).send(message);
    }

    public void onMessageRead(im.actor.model.api.Peer _peer, long startDate, long readDate) {
        Peer peer = EntityConverter.convert(_peer);

    }

    public void onMessageReadByMe(im.actor.model.api.Peer _peer, long startDate) {
        Peer peer = EntityConverter.convert(_peer);

    }

    public void onMessageReceived(im.actor.model.api.Peer _peer, long startDate, long receivedDate) {
        Peer peer = EntityConverter.convert(_peer);

    }

    public void onMessageDelete(im.actor.model.api.Peer _peer, List<Long> rids) {
        Peer peer = EntityConverter.convert(_peer);

    }

    public void onMessageSent(im.actor.model.api.Peer _peer, long rid, long date) {
        Peer peer = EntityConverter.convert(_peer);

    }

    public void onChatClear(im.actor.model.api.Peer _peer) {
        Peer peer = EntityConverter.convert(_peer);
        messenger.getMessagesModule().getDialogsActor().send(new DialogsActor.ChatClear(peer));
    }

    public void onChatDelete(im.actor.model.api.Peer _peer) {
        Peer peer = EntityConverter.convert(_peer);
        messenger.getMessagesModule().getDialogsActor().send(new DialogsActor.ChatDelete(peer));
    }


    public void onUserRegistered(int uid) {

    }
}