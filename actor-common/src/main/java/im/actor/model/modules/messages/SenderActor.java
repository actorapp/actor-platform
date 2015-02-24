package im.actor.model.modules.messages;

import java.io.IOException;

import im.actor.model.api.MessageContent;
import im.actor.model.api.OutPeer;
import im.actor.model.api.TextMessage;
import im.actor.model.api.base.SeqUpdate;
import im.actor.model.api.rpc.RequestSendMessage;
import im.actor.model.api.rpc.ResponseSeqDate;
import im.actor.model.api.updates.UpdateMessageSent;
import im.actor.model.entity.Group;
import im.actor.model.entity.Message;
import im.actor.model.entity.MessageState;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.entity.User;
import im.actor.model.entity.content.TextContent;
import im.actor.model.modules.Modules;
import im.actor.model.modules.messages.entity.PendingMessage;
import im.actor.model.modules.messages.entity.PendingMessagesStorage;
import im.actor.model.modules.utils.ModuleActor;
import im.actor.model.modules.utils.RandomUtils;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;

/**
 * Created by ex3ndr on 17.02.15.
 */
public class SenderActor extends ModuleActor {

    private static final String PREFERENCES = "sender_pending";

    private PendingMessagesStorage pendingMessages;

    public SenderActor(Modules messenger) {
        super(messenger);
    }

    @Override
    public void preStart() {
        pendingMessages = new PendingMessagesStorage();
        byte[] p = preferences().getBytes(PREFERENCES);
        if (p != null) {
            try {
                pendingMessages = PendingMessagesStorage.fromBytes(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (PendingMessage pending : pendingMessages.getPendingMessages()) {
            if (pending.getContent() instanceof TextContent) {
                performTextSend(pending.getPeer(), pending.getRid(),
                        ((TextContent) pending.getContent()).getText());
            } else {
                // TODO: Process file upload
            }
        }
    }

    public void doSendText(Peer peer, String text) {
        long rid = RandomUtils.nextRid();
        long date = System.currentTimeMillis();

        Message message = new Message(rid, date, date, myUid(), MessageState.PENDING, new TextContent(text));
        getConversationActor(peer).send(message);

        pendingMessages.getPendingMessages().add(new PendingMessage(peer, rid, new TextContent(text)));

        performTextSend(peer, rid, text);
    }

    private void performTextSend(final Peer peer, final long rid, String text) {
        OutPeer outPeer;
        final im.actor.model.api.Peer apiPeer;
        if (peer.getPeerType() == PeerType.PRIVATE) {
            User user = getUser(peer.getPeerId());
            if (user == null) {
                return;
            }

            outPeer = new OutPeer(im.actor.model.api.PeerType.PRIVATE, user.getUid(), user.getAccessHash());
            apiPeer = new im.actor.model.api.Peer(im.actor.model.api.PeerType.PRIVATE, user.getUid());
        } else if (peer.getPeerType() == PeerType.GROUP) {
            Group group = getGroup(peer.getPeerId());
            if (group == null) {
                return;
            }
            outPeer = new OutPeer(im.actor.model.api.PeerType.GROUP, group.getGroupId(), group.getAccessHash());
            apiPeer = new im.actor.model.api.Peer(im.actor.model.api.PeerType.GROUP, group.getGroupId());
        } else {
            return;
        }

        request(new RequestSendMessage(outPeer, rid, new MessageContent(0x01, new TextMessage(text, 0, new byte[0]).toByteArray())),
                new RpcCallback<ResponseSeqDate>() {
                    @Override
                    public void onResult(ResponseSeqDate response) {
                        self().send(new MessageSent(peer, rid));
                        updates().onUpdateReceived(new SeqUpdate(response.getSeq(),
                                response.getState(),
                                UpdateMessageSent.HEADER,
                                new UpdateMessageSent(apiPeer, rid, response.getDate()).toByteArray()));
                    }

                    @Override
                    public void onError(RpcException e) {
                        self().send(new MessageError(peer, rid));
                        getConversationActor(peer).send(new ConversationActor.MessageError(rid));
                    }
                });
    }

    private void onSent(Peer peer, long rid) {
        for (PendingMessage pending : pendingMessages.getPendingMessages()) {
            if (pending.getRid() == rid && pending.getPeer().equals(peer)) {
                pendingMessages.getPendingMessages().remove(pending);
                break;
            }
        }
        savePending();
    }

    private void onError(Peer peer, long rid) {
        for (PendingMessage pending : pendingMessages.getPendingMessages()) {
            if (pending.getRid() == rid && pending.getPeer().equals(peer)) {
                pendingMessages.getPendingMessages().remove(pending);
                break;
            }
        }
        savePending();
    }

    private void savePending() {
        preferences().putBytes(PREFERENCES, pendingMessages.toByteArray());
    }

    // Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof SendText) {
            SendText sendText = (SendText) message;
            doSendText(sendText.getPeer(), sendText.getText());
        } else if (message instanceof MessageSent) {
            MessageSent messageSent = (MessageSent) message;
            onSent(messageSent.getPeer(), messageSent.getRid());
        } else if (message instanceof MessageError) {
            MessageError messageError = (MessageError) message;
            onError(messageError.getPeer(), messageError.getRid());
        } else {
            drop(message);
        }
    }

    public static class SendText {
        private Peer peer;
        private String text;

        public SendText(Peer peer, String text) {
            this.peer = peer;
            this.text = text;
        }

        public Peer getPeer() {
            return peer;
        }

        public String getText() {
            return text;
        }
    }

    public static class MessageSent {
        private Peer peer;
        private long rid;

        public MessageSent(Peer peer, long rid) {
            this.peer = peer;
            this.rid = rid;
        }

        public Peer getPeer() {
            return peer;
        }

        public long getRid() {
            return rid;
        }
    }

    public static class MessageError {
        private Peer peer;
        private long rid;

        public MessageError(Peer peer, long rid) {
            this.peer = peer;
            this.rid = rid;
        }

        public Peer getPeer() {
            return peer;
        }

        public long getRid() {
            return rid;
        }
    }
}