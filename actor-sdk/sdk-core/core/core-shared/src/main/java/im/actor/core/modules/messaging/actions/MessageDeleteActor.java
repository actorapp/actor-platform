/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.messaging.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiEncryptedDeleteContent;
import im.actor.core.api.ApiPeer;
import im.actor.core.api.ApiOutPeer;
import im.actor.core.api.base.SeqUpdate;
import im.actor.core.api.rpc.RequestDeleteMessage;
import im.actor.core.api.rpc.ResponseSeq;
import im.actor.core.api.updates.UpdateMessageDelete;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.messaging.actions.entity.Delete;
import im.actor.core.modules.messaging.actions.entity.DeleteStorage;
import im.actor.core.modules.ModuleActor;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.util.RandomUtils;
import im.actor.runtime.storage.SyncKeyValue;

public class MessageDeleteActor extends ModuleActor {

    private SyncKeyValue syncKeyValue;
    private DeleteStorage deleteStorage;

    public MessageDeleteActor(ModuleContext context) {
        super(context);
        this.syncKeyValue = context.getMessagesModule().getCursorStorage();
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
        if (peer.getPeerType() == PeerType.PRIVATE_ENCRYPTED) {
            context().getEncryption().doSend(RandomUtils.nextRid(),
                    new ApiEncryptedDeleteContent(peer.getPeerId(), rids), peer.getPeerId()).then(r -> {
                if (deleteStorage.getPendingDeletions().containsKey(peer)) {
                    deleteStorage.getPendingDeletions().get(peer).getRids().removeAll(rids);
                    saveStorage();
                }
            });
        } else {
            final ApiOutPeer outPeer = buidOutPeer(peer);
            final ApiPeer apiPeer = buildApiPeer(peer);
            api(new RequestDeleteMessage(outPeer, rids)).then(r -> {
                if (deleteStorage.getPendingDeletions().containsKey(peer)) {
                    deleteStorage.getPendingDeletions().get(peer).getRids().removeAll(rids);
                    saveStorage();
                }

                updates().onUpdateReceived(new SeqUpdate(r.getSeq(), r.getState(),
                        UpdateMessageDelete.HEADER, new UpdateMessageDelete(apiPeer, rids).toByteArray()));
            });
        }
    }

    public void onDeleteMessage(Peer peer, List<Long> rids) {
        // Add to storage
        if (!deleteStorage.getPendingDeletions().containsKey(peer)) {
            deleteStorage.getPendingDeletions().put(peer, new Delete(peer, new ArrayList<Long>()));
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
            super.onReceive(message);
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
