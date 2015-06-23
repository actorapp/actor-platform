/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.messages;

import im.actor.model.api.OutPeer;
import im.actor.model.api.rpc.RequestMessageReceived;
import im.actor.model.api.rpc.ResponseVoid;
import im.actor.model.entity.Peer;
import im.actor.model.modules.Modules;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;

public class CursorReceiverActor extends CursorActor {

    public CursorReceiverActor(Modules messenger) {
        super(CURSOR_RECEIVED, messenger);
    }

    @Override
    protected void perform(final Peer peer, final long date) {
        OutPeer outPeer = buidOutPeer(peer);

        if (outPeer == null) {
            return;
        }

        request(new RequestMessageReceived(outPeer, date), new RpcCallback<ResponseVoid>() {
            @Override
            public void onResult(ResponseVoid response) {
                onCompleted(peer, date);
            }

            @Override
            public void onError(RpcException e) {
                CursorReceiverActor.this.onError(peer, date);
            }
        });
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
