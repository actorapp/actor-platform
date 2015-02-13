package im.actor.messenger.core.actors.chat;

import com.droidkit.actors.ActorCreator;
import com.droidkit.actors.ActorSelection;
import com.droidkit.actors.ActorSystem;
import com.droidkit.actors.Props;
import com.droidkit.actors.concurrency.FutureCallback;
import com.droidkit.actors.typed.TypedActor;
import com.droidkit.actors.typed.TypedCreator;
import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;
import java.util.HashMap;

import im.actor.api.scheme.HistoryMessage;
import im.actor.api.scheme.OutPeer;
import im.actor.api.scheme.PeerType;
import im.actor.api.scheme.rpc.ResponseLoadHistory;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.actors.users.UserActor;
import im.actor.messenger.model.DialogType;
import im.actor.messenger.model.DialogUids;
import im.actor.messenger.model.GroupModel;
import im.actor.messenger.model.UserModel;
import im.actor.messenger.util.io.BserPersistence;

import static im.actor.messenger.core.Core.requests;
import static im.actor.messenger.storage.KeyValueEngines.groups;
import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 01.12.14.
 */
public class ConversationHistoryActor extends TypedActor<ConversationHistoryInt> implements ConversationHistoryInt {

    private static final HashMap<Long, ConversationHistoryInt> PROXIES = new HashMap<Long, ConversationHistoryInt>();

    public static ConversationHistoryInt conv(final int type, final int id) {
        long uid = DialogUids.getDialogUid(type, id);
        synchronized (PROXIES) {
            if (!PROXIES.containsKey(uid)) {
                ConversationHistoryInt res = TypedCreator.typed(ActorSystem.system().actorOf(selection(type, id)), ConversationHistoryInt.class);
                PROXIES.put(uid, res);
            }
            return PROXIES.get(uid);
        }
    }

    private static ActorSelection selection(final int type, final int id) {
        return new ActorSelection(Props.create(ConversationHistoryActor.class, new ActorCreator<ConversationHistoryActor>() {
            @Override
            public ConversationHistoryActor create() {
                return new ConversationHistoryActor(type, id);
            }
        }), "conv_history_" + type + "_" + id);
    }

    private static final int LIMIT = 50;

    private BserPersistence<State> state;

    private int chatType;
    private int chatId;

    private boolean isLoading = false;

    public ConversationHistoryActor(int chatType, int chatId) {
        super(ConversationHistoryInt.class);
        this.chatType = chatType;
        this.chatId = chatId;
    }

    @Override
    public void preStart() {
        super.preStart();
        state = new BserPersistence<State>(AppContext.getContext(), "chat_" + DialogUids.getDialogUid(chatType, chatId) + ".ini", State.class);
        // state.setObj(new State(0, false));
        if (state.getObj() == null) {
            state.setObj(new State(0, false));
        }

        if (!state.getObj().isCompleted() && state.getObj().maxLoadedDate == 0) {
            onEndReached();
        }
    }

    @Override
    public void onEndReached() {
        if (state.getObj().isCompleted()) {
            return;
        }
        if (isLoading) {
            return;
        }
        isLoading = true;

        OutPeer peer;
        if (chatType == DialogType.TYPE_GROUP) {
            GroupModel model = groups().get(chatId);
            peer = new OutPeer(PeerType.GROUP, chatId, model.getAccessHash());
        } else if (chatType == DialogType.TYPE_USER) {
            UserModel user = users().get(chatId);
            peer = new OutPeer(PeerType.PRIVATE, chatId, user.getAccessHash());
        } else {
            return;
        }

        ask(requests().loadHistory(peer, state.getObj().getMaxLoadedDate(), LIMIT),
                new FutureCallback<ResponseLoadHistory>() {
                    @Override
                    public void onResult(final ResponseLoadHistory result) {
                        ask(UserActor.userActor().onUpdateUsers(result.getUsers()), new FutureCallback<Boolean>() {
                            @Override
                            public void onResult(Boolean result2) {
                                long maxValue = state.getObj().getMaxLoadedDate();
                                for (HistoryMessage d : result.getHistory()) {
                                    maxValue = Math.max(maxValue, d.getDate());
                                }
                                state.setObj(new State(maxValue, result.getHistory().size() < LIMIT));
                                ConversationActor.conv(chatType, chatId).onHistoryLoaded(result.getHistory());
                                isLoading = false;
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                // Just Ignore this
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        // Just Ignore this
                    }
                });
    }

    public static class State extends BserObject {

        private long maxLoadedDate;
        private boolean isCompleted;

        public State() {
        }

        public State(long maxLoadedDate, boolean isCompleted) {
            this.maxLoadedDate = maxLoadedDate;
            this.isCompleted = isCompleted;
        }

        public long getMaxLoadedDate() {
            return maxLoadedDate;
        }

        public boolean isCompleted() {
            return isCompleted;
        }

        @Override
        public void parse(BserValues values) throws IOException {
            maxLoadedDate = values.getLong(1);
            isCompleted = values.getBool(2);
        }

        @Override
        public void serialize(BserWriter writer) throws IOException {
            writer.writeLong(1, maxLoadedDate);
            writer.writeBool(2, isCompleted);
        }
    }
}
