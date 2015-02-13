package im.actor.messenger.core.actors.messages;

import com.droidkit.actors.concurrency.FutureCallback;
import com.droidkit.actors.typed.TypedActor;
import com.droidkit.engine.persistence.PersistenceMap;
import com.droidkit.engine.persistence.SerializableMap;
import com.droidkit.engine.persistence.storage.SqliteStorage;

import im.actor.api.scheme.OutPeer;
import im.actor.api.scheme.PeerType;
import im.actor.api.scheme.rpc.ResponseVoid;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.actors.base.TypedActorHolder;
import im.actor.messenger.model.DialogType;
import im.actor.messenger.model.DialogUids;
import im.actor.messenger.model.GroupModel;
import im.actor.messenger.model.UserModel;
import im.actor.messenger.storage.DbProvider;

import static im.actor.messenger.core.Core.requests;
import static im.actor.messenger.storage.KeyValueEngines.groups;
import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 16.11.14.
 */
public class PlainReceivedActor extends TypedActor<PlainReceivedInt> implements PlainReceivedInt {

    private static final TypedActorHolder<PlainReceivedInt> HOLDER = new TypedActorHolder<PlainReceivedInt>(
            PlainReceivedInt.class, PlainReceivedActor.class, "plain_receive");

    public static PlainReceivedInt plainReceive() {
        return HOLDER.get();
    }

    public PlainReceivedActor() {
        super(PlainReceivedInt.class);
    }

    private PersistenceMap<Long> read;

    private PersistenceMap<Long> pending;

    @Override
    public void preStart() {
        super.preStart();
        read = new SerializableMap<Long>(new SqliteStorage(
                DbProvider.getDatabase(AppContext.getContext()), "messages_read"));
        pending = new SerializableMap<Long>(new SqliteStorage(
                DbProvider.getDatabase(AppContext.getContext()), "messages_read_pending"));

        for (Long pendingUid : pending.keySet().toArray(new Long[0])) {
            int chatType = DialogUids.getType(pendingUid);
            int chatId = DialogUids.getId(pendingUid);
            peformMarkread(chatType, chatId, pending.get(pendingUid));
        }
    }

    @Override
    public void markReceived(int chatType, int chatId, long date) {
        long uid = DialogUids.getDialogUid(chatType, chatId);
        // Already read
        if (read.containsKey(uid)) {
            if (read.get(uid) > date) {
                return;
            }
        }

        if (pending.containsKey(uid)) {
            if (pending.get(uid) > date) {
                return;
            }
        }

        pending.put(uid, date);
        peformMarkread(chatType, chatId, date);
    }

    private void peformMarkread(int chatType, int chatId, final long date) {
        final long pendingUid = DialogUids.getDialogUid(chatType, chatId);
        if (chatType == DialogType.TYPE_USER) {
            UserModel userModel = users().get(chatId);
            if (userModel == null) {
                pending.remove(pendingUid);
                return;
            }

            ask(requests().messageReceived(new OutPeer(PeerType.PRIVATE, chatId, userModel.getAccessHash()),
                    date), new FutureCallback<ResponseVoid>() {
                @Override
                public void onResult(ResponseVoid result) {
                    Long pendingDate = pending.get(pendingUid);
                    if (pendingDate != null && pendingDate == date) {
                        pending.remove(pendingUid);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    // Just ignore error
                }
            });
        } else if (chatType == DialogType.TYPE_GROUP) {
            GroupModel groupModel = groups().get(chatId);
            if (groupModel == null) {
                pending.remove(pendingUid);
                return;
            }

            ask(requests().messageReceived(new OutPeer(PeerType.GROUP, chatId, groupModel.getAccessHash()),
                    date), new FutureCallback<ResponseVoid>() {
                @Override
                public void onResult(ResponseVoid result) {
                    Long pendingDate = pending.get(pendingUid);
                    if (pendingDate != null && pendingDate == date) {
                        pending.remove(pendingUid);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    // Just ignore error
                }
            });
        }
    }
}
