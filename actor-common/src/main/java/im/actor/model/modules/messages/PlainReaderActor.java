package im.actor.model.modules.messages;

import java.io.IOException;
import java.util.HashSet;

import im.actor.model.api.OutPeer;
import im.actor.model.api.rpc.RequestMessageRead;
import im.actor.model.api.rpc.ResponseVoid;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.entity.User;
import im.actor.model.modules.Modules;
import im.actor.model.modules.messages.entity.PlainCursor;
import im.actor.model.modules.messages.entity.PlainCursorsStorage;
import im.actor.model.modules.utils.ModuleActor;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;

/**
 * Created by ex3ndr on 17.02.15.
 */
public class PlainReaderActor extends PlainCursorActor {

    private static final String PREFERENCE = "read_plain_storage";

    public PlainReaderActor(Modules messenger) {
        super(PREFERENCE, messenger);
    }

    @Override
    protected void perform(final Peer peer, final long date) {
        OutPeer outPeer;
        if (peer.getPeerType() == PeerType.PRIVATE) {
            User user = getUser(peer.getPeerId());
            if (user == null) {
                return;
            }
            outPeer = new OutPeer(im.actor.model.api.PeerType.PRIVATE, user.getUid(), user.getAccessHash());
        } else {
            return;
        }

        request(new RequestMessageRead(outPeer, date), new RpcCallback<ResponseVoid>() {
            @Override
            public void onResult(ResponseVoid response) {
                onCompleted(peer, date);
            }

            @Override
            public void onError(RpcException e) {
                PlainReaderActor.this.onError(peer, date);
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
