package im.actor.messenger.core.actors.chat;

import com.droidkit.actors.concurrency.FutureCallback;
import com.droidkit.actors.typed.TypedActor;
import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

import im.actor.api.scheme.Dialog;
import im.actor.api.scheme.rpc.ResponseLoadDialogs;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.actors.base.TypedActorHolder;
import im.actor.messenger.core.actors.groups.GroupsActor;
import im.actor.messenger.core.actors.users.UserActor;
import im.actor.messenger.util.io.BserPersistence;

import static im.actor.messenger.core.Core.requests;

/**
 * Created by ex3ndr on 01.12.14.
 */
public class DialogsHistoryActor extends TypedActor<DialogsHistoryInt> implements DialogsHistoryInt {

    private static final TypedActorHolder<DialogsHistoryInt> HOLDER = new TypedActorHolder<DialogsHistoryInt>(DialogsHistoryInt.class,
            DialogsHistoryActor.class, "dialogs_history");

    public static DialogsHistoryInt get() {
        return HOLDER.get();
    }

    private static final int LIMIT = 50;

    private BserPersistence<State> state;

    private boolean isLoading = false;

    public DialogsHistoryActor() {
        super(DialogsHistoryInt.class);
    }


    @Override
    public void preStart() {
        super.preStart();
        state = new BserPersistence<State>(AppContext.getContext(), "dialogs.ini", State.class);
//         state.setObj(new State(0, false));
        if (state.getObj() == null) {
            state.setObj(new State(0, false));
        }

    }

    @Override
    public void onAuthenticated() {
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

        ask(requests().loadDialogs(state.getObj().getMaxLoadedDate(), LIMIT), new FutureCallback<ResponseLoadDialogs>() {
            @Override
            public void onResult(final ResponseLoadDialogs result) {
                ask(UserActor.userActor().onUpdateUsers(result.getUsers()), new FutureCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean result2) {
                        ask(GroupsActor.groupUpdates().onUpdateGroups(result.getGroups()), new FutureCallback<Boolean>() {
                            @Override
                            public void onResult(Boolean result2) {
                                long maxValue = state.getObj().getMaxLoadedDate();
                                for (Dialog d : result.getDialogs()) {
                                    maxValue = Math.max(maxValue, d.getSortDate());
                                }
                                state.setObj(new State(maxValue, result.getDialogs().size() < LIMIT));
                                DialogsActor.dialogs().onDialogsHistoryLoaded(result.getDialogs());
                                isLoading = false;
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                // Just ignore this
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        // Just ignore this
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                // Just ignore this
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
