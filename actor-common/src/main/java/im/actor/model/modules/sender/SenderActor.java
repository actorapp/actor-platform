package im.actor.model.modules.sender;

import im.actor.model.droidkit.actors.conf.EnvConfig;
import im.actor.model.Messenger;
import im.actor.model.api.*;
import im.actor.model.api.rpc.RequestSendMessage;
import im.actor.model.api.rpc.ResponseSeqDate;
import im.actor.model.api.updates.UpdateMessageSent;
import im.actor.model.entity.*;
import im.actor.model.entity.MessageState;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.entity.User;
import im.actor.model.entity.content.AbsContent;
import im.actor.model.entity.content.TextContent;
import im.actor.model.modules.utils.ModuleActor;
import im.actor.model.modules.utils.RandomUtils;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;

/**
 * Created by ex3ndr on 11.02.15.
 */
public class SenderActor extends ModuleActor {

    public SenderActor(Messenger messenger) {
        super(messenger);
    }

    @Override
    public void preStart() {
        if (getMessenger().getConfiguration().isPersistMessages()) {
            // TODO: Check persistent pending messages
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof SendMessage) {
            SendMessage sendMessage = (SendMessage) message;
            sendMessage(sendMessage.peer, RandomUtils.nextRid(),
                    EnvConfig.getJavaFactory().getCurrentTime(),
                    sendMessage.content);
        } else if (message instanceof MessageSent) {
            // TODO: Implement
        }
    }

    private void sendMessage(final Peer peer, final long rid, long time, AbsContent content) {
        final OutPeer outPeer;
        final im.actor.model.api.Peer apiPeer;
        if (peer.getPeerType() == PeerType.PRIVATE) {
            User user = getMessenger().getUsers().getValue(peer.getUid());
            if (user == null) {
                return;
            }
            outPeer = new OutPeer(im.actor.model.api.PeerType.PRIVATE, peer.getPeerId(), user.getAccessHash());
            apiPeer = new im.actor.model.api.Peer(im.actor.model.api.PeerType.PRIVATE, peer.getPeerId());
        } else {
            return;
        }

        MessageContent outContent;
        if (content instanceof TextContent) {
            outContent = new MessageContent(0x01, new TextMessage(((TextContent) content).getText(), 0, new byte[0]).toByteArray());
        } else {
            return;
        }

        getMessenger().getMessagesModule().getConversationActor(peer).send(new Message(rid,
                time, time, getMessenger().myUid(), MessageState.PENDING, content));

        request(new RequestSendMessage(outPeer, rid, outContent),
                new RpcCallback<ResponseSeqDate>() {
                    @Override
                    public void onResult(ResponseSeqDate response) {
                        getMessenger().getUpdatesModule()
                                .onUpdateReceived(new UpdateMessageSent(apiPeer, rid, response.getDate()));
                    }

                    @Override
                    public void onError(RpcException e) {

                    }
                });
    }

    public static class SendMessage {
        private Peer peer;
        private AbsContent content;

        public SendMessage(Peer peer, AbsContent content) {
            this.peer = peer;
            this.content = content;
        }

        public Peer getPeer() {
            return peer;
        }

        public AbsContent getContent() {
            return content;
        }
    }

    public static class MessageSent {
        private long rid;
        private long date;

        public MessageSent(long rid, long date) {
            this.rid = rid;
            this.date = date;
        }

        public long getRid() {
            return rid;
        }

        public long getDate() {
            return date;
        }
    }
}
