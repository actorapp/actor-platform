/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.sequence;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiUpdateOptimization;
import im.actor.core.api.ApiUser;
import im.actor.core.api.base.FatSeqUpdate;
import im.actor.core.api.base.SeqUpdate;
import im.actor.core.api.base.SeqUpdateTooLong;
import im.actor.core.api.rpc.RequestGetDifference;
import im.actor.core.api.rpc.RequestGetState;
import im.actor.core.api.rpc.ResponseGetDifference;
import im.actor.core.api.rpc.ResponseSeq;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.updates.internal.ExecuteAfter;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.util.ModuleActor;
import im.actor.runtime.Log;
import im.actor.runtime.function.Constructor;
import im.actor.runtime.function.Consumer;

public class SequenceActor extends ModuleActor {

    public static Constructor<SequenceActor> CONSTRUCTOR(final ModuleContext context) {
        return new Constructor<SequenceActor>() {
            @Override
            public SequenceActor create() {
                return new SequenceActor(context);
            }
        };
    }

    private static final String TAG = "Updates";
    private static final int INVALIDATE_GAP = 2000;// 2 Secs
    private static final int INVALIDATE_MAX_SEC_HOLE = 10;

    private static final String KEY_SEQ = "updates_seq";
    private static final String KEY_STATE = "updates_state";

    private ArrayList<ExecuteAfter> pendingRunnables = new ArrayList<>();

    private boolean isValidated = true;
    private boolean isTimerStarted = false;

    private int seq;
    private byte[] state;
    private int finishedSeq;
    private byte[] finishedState;

    private SequenceHandlerInt handler;

    public SequenceActor(ModuleContext modules) {
        super(modules);
    }

    @Override
    public void preStart() {
        seq = preferences().getInt(KEY_SEQ, -1);
        finishedSeq = seq;
        state = preferences().getBytes(KEY_STATE);

        handler = context().getUpdatesModule().getUpdateHandler();

        self().send(new Invalidate());
    }

    private void onPushSeqReceived(int seq) {
        if (seq <= this.seq) {
            Log.d(TAG, "Ignored PushSeq {seq:" + seq + "}");
        } else {
            Log.w(TAG, "External Out of sequence: starting timer for invalidation");
            self().sendOnce(new ForceInvalidate(), INVALIDATE_GAP);
        }
    }

    @Deprecated
    private void onExecuteAfter(ExecuteAfter after) {
        if (after.getSeq() <= this.seq) {
            after.getRunnable().run();
        } else {
            pendingRunnables.add(after);
        }
    }

    private void onUpdateReceived(final int seq, final byte[] state, int type, byte[] body, List<ApiUser> users,
                                  List<ApiGroup> groups) {

        // Checking sequence
        if (seq <= this.seq) {
            Log.d(TAG, "Ignored SeqUpdate {seq:" + seq + ", currentSeq: " + this.seq + "}");
            return;
        }
        Log.d(TAG, "SeqUpdate {seq:" + seq + "}");

        if (!isValidated) {
            Log.d(TAG, "Stashing update");
            stash();
            return;
        }

        if (seq != this.seq + 1) {
            stash();

            if (seq - this.seq > INVALIDATE_MAX_SEC_HOLE) {
                Log.w(TAG, "Out of sequence: Too big hole. Force invalidate immediately");
                forceInvalidate();
            }

            if (isTimerStarted) {
                Log.w(TAG, "Out of sequence: timer already started");
            } else {
                Log.w(TAG, "Out of sequence: starting timer for invalidation");
                startInvalidationTimer();
            }

            return;
        }

        Log.d(TAG, "Handling update #" + seq);
        handler.onSeqUpdate(type, body, users, groups).then(new Consumer<SequenceHandlerActor.UpdateProcessed>() {
            @Override
            public void apply(SequenceHandlerActor.UpdateProcessed updateProcessed) {
                Log.d(TAG, "Handling update ended #" + seq);
                onUpdatesApplied(seq, state);
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                SequenceActor.this.seq = finishedSeq;
                SequenceActor.this.state = finishedState;
                invalidate();
            }
        }).done(self());

        // Saving memory-only state
        this.seq = seq;
        this.state = state;

        unstashAll();

        stopInvalidationTimer();
    }

    private void invalidate() {
        if (!isValidated) {
            return;
        }
        isValidated = false;

        if (seq < 0) {
            Log.d(TAG, "Loading fresh state...");
            ArrayList<ApiUpdateOptimization> optimizations = new ArrayList<>();
            optimizations.add(ApiUpdateOptimization.STRIP_ENTITIES);
            request(new RequestGetState(optimizations), new RpcCallback<ResponseSeq>() {
                @Override
                public void onResult(ResponseSeq response) {
                    if (isValidated) {
                        return;
                    }

                    Log.d(TAG, "State loaded {seq=" + seq + "}");

                    seq = response.getSeq();
                    state = response.getState();
                    persistState(seq, state);
                    onBecomeValid(response.getSeq(), response.getState());
                }

                @Override
                public void onError(RpcException e) {
                    if (isValidated) {
                        return;
                    }
                    isValidated = true;
                    invalidate();
                }
            });
        } else {
            Log.d(TAG, "Loading difference...");
            onUpdateStarted();
            final long loadStart = im.actor.runtime.Runtime.getCurrentTime();
            ArrayList<ApiUpdateOptimization> optimizations = new ArrayList<>();
            optimizations.add(ApiUpdateOptimization.STRIP_ENTITIES);
            request(new RequestGetDifference(seq, state, optimizations), new RpcCallback<ResponseGetDifference>() {
                @Override
                public void onResult(final ResponseGetDifference response) {
                    if (isValidated) {
                        return;
                    }

                    Log.d(TAG, "Difference loaded {seq=" + response.getSeq() + "} in "
                            + (im.actor.runtime.Runtime.getCurrentTime() - loadStart) + " ms, " +
                            "userRefs: " + response.getUsersRefs().size() + ", " +
                            "groupRefs: " + response.getGroupsRefs().size());

                    handler.onDifferenceUpdate(response).then(new Consumer<SequenceHandlerActor.UpdateProcessed>() {
                        @Override
                        public void apply(SequenceHandlerActor.UpdateProcessed updateProcessed) {
                            onUpdatesApplied(response.getSeq(), response.getState());
                        }
                    }).done(self());

                    onBecomeValid(response.getSeq(), response.getState());

                    if (response.needMore()) {
                        invalidate();
                    } else {
                        onUpdateEnded();
                    }
                }

                @Override
                public void onError(RpcException e) {
                    if (isValidated) {
                        return;
                    }
                    isValidated = true;

                    invalidate();
                }
            });
        }
    }

    private void onUpdatesApplied(int seq, byte[] state) {
        if (seq > finishedSeq) {
            persistState(seq, state);
            if (this.seq == seq) {
                Log.d(TAG, "All updates applied {seq:" + seq + "}");
            } else {
                Log.d(TAG, "Updates applied {seq:" + seq + ", finishedSeq: " + finishedSeq + "}");
            }
            checkRunnables();
        }
    }

    private void onBecomeValid(int seq, byte[] state) {
        isValidated = true;
        this.seq = seq;
        this.state = state;
        unstashAll();
        stopInvalidationTimer();
    }

    private void persistState(int seq, byte[] state) {
        finishedSeq = seq;
        finishedState = state;
        preferences().putInt(KEY_SEQ, seq);
        preferences().putBytes(KEY_STATE, state);
    }

    private void checkRunnables() {
        if (pendingRunnables.size() > 0) {
            for (ExecuteAfter e : pendingRunnables.toArray(new ExecuteAfter[pendingRunnables.size()])) {
                if (e.getSeq() <= this.finishedSeq) {
                    e.getRunnable().run();
                    pendingRunnables.remove(e);
                }
            }
        }
    }

    //
    // UI Notifications
    //

    private void onUpdateStarted() {
        context().getAppStateModule().getAppStateVM().getIsSyncing().change(true);
    }

    private void onUpdateEnded() {
        context().getAppStateModule().getAppStateVM().getIsSyncing().change(false);
    }

    //
    // Invalidation Timer
    //

    private void stopInvalidationTimer() {
        isTimerStarted = false;
        // Faaaaaar away
        self().sendOnce(new ForceInvalidate(), 24 * 60 * 60 * 1000L);
    }

    private void startInvalidationTimer() {
        if (!isTimerStarted) {
            self().sendOnce(new ForceInvalidate(), INVALIDATE_GAP);
            isTimerStarted = true;
        }
    }

    private void forceInvalidate() {
        self().sendOnce(new ForceInvalidate());
        isTimerStarted = false;
    }

    //
    // Messages
    //

    @Override
    public void onReceive(Object message) {
        if (message instanceof Invalidate
                || message instanceof SeqUpdateTooLong
                || message instanceof ForceInvalidate) {
            if (!isValidated) {
                return;
            }
            invalidate();
        } else if (message instanceof SeqUpdate || message instanceof FatSeqUpdate) {
            if (!isValidated) {
                stash();
                return;
            }

            int seq;
            byte[] state;
            int type;
            byte[] body;
            List<ApiUser> users = null;
            List<ApiGroup> groups = null;
            if (message instanceof SeqUpdate) {
                seq = ((SeqUpdate) message).getSeq();
                state = ((SeqUpdate) message).getState();
                type = ((SeqUpdate) message).getUpdateHeader();
                body = ((SeqUpdate) message).getUpdate();
            } else {
                seq = ((FatSeqUpdate) message).getSeq();
                state = ((FatSeqUpdate) message).getState();
                type = ((FatSeqUpdate) message).getUpdateHeader();
                body = ((FatSeqUpdate) message).getUpdate();
                users = ((FatSeqUpdate) message).getUsers();
                groups = ((FatSeqUpdate) message).getGroups();
            }

            onUpdateReceived(seq, state, type, body, users, groups);
        } else if (message instanceof ExecuteAfter) {
            if (!isValidated) {
                stash();
                return;
            }
            onExecuteAfter((ExecuteAfter) message);
        } else if (message instanceof PushSeq) {
            if (!isValidated) {
                stash();
                return;
            }
            onPushSeqReceived(((PushSeq) message).seq);
        } else {
            drop(message);
        }
    }

    public static class ForceInvalidate {

    }

    public static class Invalidate {

    }

    public static class PushSeq {
        private int seq;

        public PushSeq(int seq) {
            this.seq = seq;
        }
    }
}
