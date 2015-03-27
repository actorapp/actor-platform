package im.actor.model.modules.messages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.model.api.OutPeer;
import im.actor.model.api.rpc.RequestDeleteMessage;
import im.actor.model.api.rpc.ResponseVoid;
import im.actor.model.droidkit.engine.SyncKeyValue;
import im.actor.model.entity.Peer;
import im.actor.model.modules.Modules;
import im.actor.model.modules.messages.entity.Delete;
import im.actor.model.modules.messages.entity.DeleteStorage;
import im.actor.model.modules.utils.ModuleActor;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;

/**
 * Created by ex3ndr on 26.03.15.
 */
public class MessageDeleteActor extends ModuleActor {

    private SyncKeyValue syncKeyValue;
    private DeleteStorage deleteStorage;

    public MessageDeleteActor(Modules modules) {
        super(modules);
        this.syncKeyValue = modules.getMessagesModule().getCursorStorage();
    }

    @Override
    public void preStart() {
        super.preStart();
        byte[] data = syncKeyValue.get(CURSOR_DELETE);
        if (data != null) {
            try {
                deleteStorage = DeleteStorage.fromBytes(data);
            } catch (IOException e) {
                e.printStackTrace();
                deleteStorage = new DeleteStorage();
            }
        } else {
            deleteStorage = new DeleteStorage();
        }

        for (Peer peer : deleteStorage.getPendingDeletions().keySet()) {
            Delete delete = deleteStorage.getPendingDeletions().get(peer);
            if (delete.getRids().size() > 0) {
                performDelete(peer, delete.getRids());
            }
        }
    }

    void saveStorage() {
        syncKeyValue.put(CURSOR_DELETE, deleteStorage.toByteArray());
    }

    public void performDelete(final Peer peer, final List<Long> rids) {
        final OutPeer outPeer = buidOutPeer(peer);
        request(new RequestDeleteMessage(outPeer, rids), new RpcCallback<ResponseVoid>() {
            @Override
            public void onResult(ResponseVoid response) {
                if (deleteStorage.getPendingDeletions().containsKey(peer)) {
                    deleteStorage.getPendingDeletions().get(peer).getRids().removeAll(rids);
                    saveStorage();
                }
                // TODO: Fix API?
                // updates().onUpdateReceived(new SeqUpdate());
            }

            @Override
            public void onError(RpcException e) {

            }
        });
    }

    public void onDeleteMessage(Peer peer, List<Long> rids) {
        // Add to storage
        if (!deleteStorage.getPendingDeletions().containsKey(peer)) {
            deleteStorage.getPendingDeletions().put(peer, new Delete(peer,new ArrayList<Long>()));
        }
        deleteStorage.getPendingDeletions().get(peer).getRids().addAll(rids);
        saveStorage();

        // Perform deletion
        performDelete(peer, rids);
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof DeleteMessage) {
            DeleteMessage deleteMessage = (DeleteMessage) message;
            ArrayList<Long> rids = new ArrayList<Long>();
            for (long l : deleteMessage.getRids()) {
                rids.add(l);
            }
            onDeleteMessage(deleteMessage.getPeer(), rids);
        } else {
            drop(message);
        }
    }

    public static class DeleteMessage {
        private Peer peer;
        private long[] rids;

        public DeleteMessage(Peer peer, long[] rids) {
            this.peer = peer;
            this.rids = rids;
        }

        public Peer getPeer() {
            return peer;
        }

        public long[] getRids() {
            return rids;
        }
    }
}
