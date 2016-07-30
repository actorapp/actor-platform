/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.messaging.actions;

import im.actor.core.api.ApiEncryptedRead;
import im.actor.core.api.ApiOutPeer;
import im.actor.core.api.rpc.RequestMessageRead;
import im.actor.core.api.rpc.ResponseVoid;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.modules.ModuleContext;
import im.actor.core.util.RandomUtils;
import im.actor.runtime.actors.messages.Void;

public class CursorReaderActor extends CursorActor {

    // j2objc workaround
    private static final ResponseVoid DUMB = null;
    private static final Long DUMB2 = null;

    public CursorReaderActor(ModuleContext context) {
        super(CURSOR_READ, context);
    }

    @Override
    protected void perform(final Peer peer, final long date) {
        ApiOutPeer outPeer = buidOutPeer(peer);
        if (outPeer == null) {
            return;
        }

        if (peer.getPeerType() == PeerType.PRIVATE_ENCRYPTED) {
            context().getEncryption().doSend(RandomUtils.nextRid(),
                    new ApiEncryptedRead(peer.getPeerId(), date), peer.getPeerId()).then(r -> {
                onCompleted(peer, date);
            }).failure(e -> {
                onError(peer, date);
            });
        } else {
            api(new RequestMessageRead(outPeer, date)).then(responseVoid -> {
                onCompleted(peer, date);
            }).failure(e -> {
                onError(peer, date);
            });
        }
    }

    // Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof MarkRead) {
            MarkRead markRead = (MarkRead) message;
            moveCursor(markRead.getPeer(), markRead.getDate());
        } else {
            super.onReceive(message);
        }
    }

    public static class MarkRead {
        private Peer peer;
        private long date;

        public MarkRead(Peer peer, long date) {
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
