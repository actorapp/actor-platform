/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.messaging.actions;

import im.actor.core.api.ApiEncryptedReceived;
import im.actor.core.api.ApiOutPeer;
import im.actor.core.api.rpc.RequestMessageReceived;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.modules.ModuleContext;
import im.actor.core.util.RandomUtils;

public class CursorReceiverActor extends CursorActor {

    public CursorReceiverActor(ModuleContext context) {
        super(CURSOR_RECEIVED, context);
    }

    @Override
    protected void perform(final Peer peer, final long date) {
        ApiOutPeer outPeer = buidOutPeer(peer);
        if (outPeer == null) {
            return;
        }

        if (peer.getPeerType() == PeerType.PRIVATE_ENCRYPTED) {
            context().getEncryption().doSend(RandomUtils.nextRid(),
                    new ApiEncryptedReceived(peer.getPeerId(), date), peer.getPeerId()).then(r -> {
                onCompleted(peer, date);
            }).failure(e -> {
                onError(peer, date);
            });
        } else {
            api(new RequestMessageReceived(outPeer, date)).then(responseVoid -> {
                onCompleted(peer, date);
            }).failure(e -> {
                onError(peer, date);
            });
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof MarkReceived) {
            MarkReceived received = (MarkReceived) message;
            moveCursor(received.getPeer(), received.getDate());
        } else {
            super.onReceive(message);
        }
    }

    public static class MarkReceived {
        private Peer peer;
        private long date;

        public MarkReceived(Peer peer, long date) {
            this.peer = peer;
            this.date = date;
        }

        public Peer getPeer() {
            return peer;
        }

        public long getDate() {
            return date;
        }
    }
}
