package im.actor.messenger.core.actors.messages;

import com.droidkit.actors.ActorSelection;
import com.droidkit.actors.Props;
import com.droidkit.actors.concurrency.FutureCallback;

import java.util.ArrayList;

import im.actor.api.scheme.OutPeer;
import im.actor.api.scheme.PeerType;
import im.actor.api.scheme.rpc.ResponseVoid;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.actors.base.PendingActor;
import im.actor.messenger.model.DialogType;
import im.actor.messenger.model.GroupModel;
import im.actor.messenger.model.UserModel;
import im.actor.messenger.storage.DbProvider;
import im.actor.messenger.util.Logger;

import static im.actor.messenger.core.Core.requests;
import static im.actor.messenger.storage.KeyValueEngines.groups;
import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 26.09.14.
 */
public class DeleteMessagesActor extends PendingActor<MessageAction> {

    public static ActorSelection messageReader() {
        return new ActorSelection(Props.create(DeleteMessagesActor.class), "message_delete");
    }

    private static final String TAG = "DeleteMessagesActor";

    public DeleteMessagesActor() {
        super("delete", DbProvider.getDatabase(AppContext.getContext()));
    }

    @Override
    public void onReceive(Object message) {
        super.onReceive(message);
        if (message instanceof Delete) {
            final Delete confirm = (Delete) message;
            addAction(new MessageAction(confirm.type, confirm.id, confirm.rid));
        }
    }

    @Override
    protected void performAction(final MessageAction action) {
        if (action.getChatType() != DialogType.TYPE_USER &&
                action.getChatType() != DialogType.TYPE_GROUP) {
            onActionCompleted(action);
            return;
        }

        Logger.d(TAG, "Sending delete for #" + action.getRid());
        OutPeer peer;
        if (action.getChatType() == DialogType.TYPE_USER) {
            UserModel user = users().get(action.getChatId());
            if (user == null) {
                onActionCompleted(action);
                return;
            }
            peer = new OutPeer(PeerType.PRIVATE, user.getId(), user.getAccessHash());
        } else {
            GroupModel group = groups().get(action.getChatId());
            if (group == null) {
                onActionCompleted(action);
                return;
            }
            peer = new OutPeer(PeerType.GROUP, group.getChatId(), group.getAccessHash());
        }
        ArrayList<Long> delete = new ArrayList<Long>();
        delete.add(action.getRid());
        ask(requests().deleteMessage(peer, delete), new FutureCallback<ResponseVoid>() {
            @Override
            public void onResult(ResponseVoid result) {
                Logger.d(TAG, "Message Deleted #" + action.getRid());
                onActionCompleted(action);
            }

            @Override
            public void onError(Throwable throwable) {
                Logger.d(TAG, "Message Deleted error #" + action.getRid() + " " + throwable);
                onActionCompleted(action);
            }
        });
    }

    public static class Delete {
        private int type;
        private int id;
        private long rid;

        public Delete(int type, int id, long rid) {
            this.type = type;
            this.id = id;
            this.rid = rid;
        }

        public int getType() {
            return type;
        }

        public int getId() {
            return id;
        }

        public long getRid() {
            return rid;
        }
    }
}
