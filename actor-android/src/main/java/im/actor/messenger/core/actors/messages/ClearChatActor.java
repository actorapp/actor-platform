package im.actor.messenger.core.actors.messages;

import com.droidkit.actors.Actor;
import com.droidkit.actors.ActorSelection;
import com.droidkit.actors.Props;
import com.droidkit.actors.concurrency.FutureCallback;
import com.droidkit.engine.persistence.PersistenceMap;
import com.droidkit.engine.persistence.SerializableMap;
import com.droidkit.engine.persistence.storage.SqliteStorage;

import java.io.Serializable;

import im.actor.api.scheme.OutPeer;
import im.actor.api.scheme.Peer;
import im.actor.api.scheme.PeerType;
import im.actor.api.scheme.rpc.ResponseSeq;
import im.actor.api.scheme.updates.UpdateChatClear;
import im.actor.api.scheme.updates.UpdateChatDelete;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.actors.api.SequenceActor;
import im.actor.messenger.model.DialogType;
import im.actor.messenger.model.DialogUids;
import im.actor.messenger.model.GroupModel;
import im.actor.messenger.model.UserModel;
import im.actor.messenger.storage.DbProvider;
import im.actor.messenger.util.Logger;

import static im.actor.messenger.core.Core.requests;
import static im.actor.messenger.storage.KeyValueEngines.groups;
import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 25.11.14.
 */
public class ClearChatActor extends Actor {
    public static ActorSelection clearChat() {
        return new ActorSelection(Props.create(ClearChatActor.class), "chat_clear");
    }

    private static final String TAG = "ClearChat";

    private PersistenceMap<Boolean> deletions;

    @Override
    public void preStart() {
        deletions =
                new SerializableMap<Boolean>(
                        new SqliteStorage(DbProvider.getDatabase(AppContext.getContext()), "chat_deletions"));
        for (Long uid : deletions.keySet()) {
            int type = DialogUids.getType(uid);
            int id = DialogUids.getId(uid);
            self().send(new PerformDeletion(type, id, deletions.get(uid)));
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof PerformDeletion) {
            PerformDeletion delete = ((PerformDeletion) message);
            final long uid = DialogUids.getDialogUid(delete.chatType, delete.chatId);
            Logger.d(TAG, "Performing Deletion " + uid + ", isDelete: " + delete.isDeletion);
            OutPeer peer;
            final Peer fPeer;
            if (delete.chatType == DialogType.TYPE_USER) {
                UserModel userModel = users().get(delete.chatId);
                if (userModel == null) {
                    deletions.remove(uid);
                    return;
                }

                peer = new OutPeer(PeerType.PRIVATE, delete.chatId, userModel.getAccessHash());
                fPeer = new Peer(PeerType.PRIVATE, delete.chatId);
            } else if (delete.chatType == DialogType.TYPE_GROUP) {
                GroupModel groupModel = groups().get(delete.chatId);
                if (groupModel == null) {
                    deletions.remove(uid);
                    return;
                }
                peer = new OutPeer(PeerType.GROUP, delete.chatId, groupModel.getAccessHash());
                fPeer = new Peer(PeerType.GROUP, delete.chatId);
            } else {
                deletions.remove(uid);
                return;
            }

            if (delete.isDeletion) {
                ask(requests().deleteChat(peer), new FutureCallback<ResponseSeq>() {
                    @Override
                    public void onResult(ResponseSeq result) {
                        system().actorOf(SequenceActor.sequence())
                                .send(new SequenceActor.SeqUpdate(result.getSeq(), result.getState(),
                                        new UpdateChatDelete(fPeer)));
                        deletions.remove(uid);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        // Just ignore this
                        throwable.printStackTrace();
                    }
                });
            } else {
                ask(requests().clearChat(peer), new FutureCallback<ResponseSeq>() {
                    @Override
                    public void onResult(ResponseSeq result) {
                        system().actorOf(SequenceActor.sequence())
                                .send(new SequenceActor.SeqUpdate(result.getSeq(), result.getState(),
                                        new UpdateChatClear(fPeer)));
                        deletions.remove(uid);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        // Just ignore this
                        throwable.printStackTrace();
                    }
                });
            }
        } else if (message instanceof DeleteChat) {
            DeleteChat delete = ((DeleteChat) message);
            long uid = DialogUids.getDialogUid(delete.chatType, delete.chatId);
            if (deletions.containsKey(uid)) {
                return;
            }
            deletions.put(uid, true);
            self().send(new PerformDeletion(delete.chatType, delete.chatId, true));
        } else if (message instanceof ClearChat) {
            ClearChat clear = ((ClearChat) message);
            long uid = DialogUids.getDialogUid(clear.chatType, clear.chatId);
            if (deletions.containsKey(uid)) {
                return;
            }
            deletions.put(uid, false);
            self().send(new PerformDeletion(clear.chatType, clear.chatId, false));
        }
    }

    private static class PerformDeletion implements Serializable {
        private int chatType;
        private int chatId;
        private boolean isDeletion;

        private PerformDeletion(int chatType, int chatId, boolean isDeletion) {
            this.chatType = chatType;
            this.chatId = chatId;
            this.isDeletion = isDeletion;
        }
    }

    public static class DeleteChat {
        private int chatType;
        private int chatId;

        public DeleteChat(int chatType, int chatId) {
            this.chatType = chatType;
            this.chatId = chatId;
        }
    }

    public static class ClearChat {
        private int chatType;
        private int chatId;

        public ClearChat(int chatType, int chatId) {
            this.chatType = chatType;
            this.chatId = chatId;
        }
    }
}