/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.messages;

import im.actor.core.api.OutPeer;
import im.actor.core.api.rpc.RequestMessageRead;
import im.actor.core.api.rpc.ResponseVoid;
import im.actor.core.entity.PeerEntity;
import im.actor.core.modules.ModuleContext;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;

public class CursorReaderActor extends CursorActor {

    public CursorReaderActor(ModuleContext context) {
        super(CURSOR_READ, context);
    }

    @Override
    protected void perform(final PeerEntity peer, final long date) {
        OutPeer outPeer = buidOutPeer(peer);
        if (outPeer == null) {
            return;
        }

        request(new RequestMessageRead(outPeer, date), new RpcCallback<ResponseVoid>() {
            @Override
            public void onResult(ResponseVoid response) {
                onCompleted(peer, date);
            }

            @Override
            public void onError(RpcException e) {
                CursorReaderActor.this.onError(peer, date);
            }
        });
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
        private PeerEntity peer;
        private long date;

        public MarkRead(PeerEntity peer, long date) {
            this.peer = peer;
            this.date = date;
        }

        public PeerEntity getPeer() {
            return peer;
        }

        public long getDate() {
            return date;
        }
    }
}
