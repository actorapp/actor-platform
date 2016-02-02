/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.sequence;

import java.util.ArrayList;
import java.util.HashMap;
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

    private HashMap<Integer, Object> further = new HashMap<>();

    private ArrayList<ExecuteAfter> pendingRunnables = new ArrayList<>();

    private boolean isValidated = true;
    private boolean isTimerStarted = false;

    private int seq;
    private byte[] state;

    private int finishedSeq;

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

    private void onExecuteAfter(ExecuteAfter after) {
        if (after.getSeq() <= this.seq) {
            after.getRunnable().run();
        } else {
            pendingRunnables.add(after);
        }
    }

    private void onUpdateReceived(Object u) {
        // Building parameters
        final int seq;
        final byte[] state;
        int type;
        byte[] body;
        if (u instanceof SeqUpdate) {
            seq = ((SeqUpdate) u).getSeq();
            state = ((SeqUpdate) u).getState();
            type = ((SeqUpdate) u).getUpdateHeader();
            body = ((SeqUpdate) u).getUpdate();
        } else if (u instanceof FatSeqUpdate) {
            seq = ((FatSeqUpdate) u).getSeq();
            state = ((FatSeqUpdate) u).getState();
            type = ((FatSeqUpdate) u).getUpdateHeader();
            body = ((FatSeqUpdate) u).getUpdate();
        } else {
            throw new RuntimeException();
        }

        // Checking sequence
        if (seq <= this.seq) {
            Log.d(TAG, "Ignored SeqUpdate {seq:" + seq + ", currentSeq: " + this.seq + "}");
            return;
        }
        Log.d(TAG, "SeqUpdate {seq:" + seq + "}");

        if (!isValidated) {
            Log.d(TAG, "Caching in further map");
            further.put(seq, u);
            return;
        }

        if (seq != this.seq + 1) {
            further.put(seq, u);

            if (seq - this.seq > INVALIDATE_MAX_SEC_HOLE) {
                Log.w(TAG, "Out of sequence: Too big hole. Force invalidate immediately");
                self().sendOnce(new ForceInvalidate());
                return;
            }

            if (isTimerStarted) {
                Log.w(TAG, "Out of sequence: timer already started");
            } else {
                Log.w(TAG, "Out of sequence: starting timer for invalidation");
                self().sendOnce(new ForceInvalidate(), INVALIDATE_GAP);
                isTimerStarted = true;
            }

            return;
        }

        List<ApiUser> users = null;
        List<ApiGroup> groups = null;
        if (u instanceof FatSeqUpdate) {
            users = ((FatSeqUpdate) u).getUsers();
            groups = ((FatSeqUpdate) u).getGroups();
        }
        Log.d(TAG, "Handling update #" + seq);
        handler.onSeqUpdate(type, body, users, groups).then(new Consumer<SequenceHandlerActor.UpdateProcessed>() {
            @Override
            public void apply(SequenceHandlerActor.UpdateProcessed updateProcessed) {
                onUpdatesApplied(seq, state);
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                // TODO?
            }
        }).done(self());

        // Saving memory-only state
        this.seq = seq;
        this.state = state;

        checkFuture();

        // Faaaaaar away
        isTimerStarted = false;
        self().sendOnce(new ForceInvalidate(), 24 * 60 * 60 * 1000L);
    }

    private void invalidate() {
        if (!isValidated) {
            return;
        }
        isValidated = false;

        if (seq < 0) {
            Log.d(TAG, "Loading fresh state...");
            request(new RequestGetState(new ArrayList<ApiUpdateOptimization>()), new RpcCallback<ResponseSeq>() {
                @Override
                public void onResult(ResponseSeq response) {
                    if (isValidated) {
                        return;
                    }

                    seq = response.getSeq();
                    finishedSeq = seq;
                    state = response.getState();

                    isValidated = true;

                    preferences().putInt(KEY_SEQ, seq);
                    preferences().putBytes(KEY_STATE, state);

                    Log.d(TAG, "State loaded {seq=" + seq + "}");

                    checkRunnables();
                    checkFuture();

                    // Faaaaaar away
                    isTimerStarted = false;
                    self().sendOnce(new ForceInvalidate(), 24 * 60 * 60 * 1000L);
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
            request(new RequestGetDifference(seq, state, new ArrayList<ApiUpdateOptimization>()), new RpcCallback<ResponseGetDifference>() {
                @Override
                public void onResult(final ResponseGetDifference response) {
                    if (isValidated) {
                        return;
                    }

                    Log.d(TAG, "Difference loaded {seq=" + response.getSeq() + "} in "
                            + (im.actor.runtime.Runtime.getCurrentTime() - loadStart) + " ms");

                    // checkRunnables();

                    seq = response.getSeq();
                    state = response.getState();

                    handler.onDifferenceUpdate(response).then(new Consumer<SequenceHandlerActor.UpdateProcessed>() {
                        @Override
                        public void apply(SequenceHandlerActor.UpdateProcessed updateProcessed) {
                            onUpdatesApplied(response.getSeq(), response.getState());
                        }
                    }).failure(new Consumer<Exception>() {
                        @Override
                        public void apply(Exception e) {
                            // TODO?
                        }
                    }).done(self());

                    isValidated = true;

                    checkFuture();

                    // Faaaaaar away
                    isTimerStarted = false;
                    self().sendOnce(new ForceInvalidate(), 24 * 60 * 60 * 1000L);

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
        finishedSeq = seq;
        preferences().putInt(KEY_SEQ, seq);
        preferences().putBytes(KEY_STATE, state);
        checkRunnables();
    }

    private void checkFuture() {
        for (int i = seq + 1; ; i++) {
            if (further.containsKey(i)) {
                onReceive(further.remove(i));
            } else {
                break;
            }
        }
        for (Integer key : further.keySet().toArray(new Integer[0])) {
            if (key <= seq) {
                further.remove(key);
            }
        }
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
    // Messages
    //

    @Override
    public void onReceive(Object message) {
        if (message instanceof Invalidate
                || message instanceof SeqUpdateTooLong
                || message instanceof ForceInvalidate) {
            invalidate();
        } else if (message instanceof SeqUpdate || message instanceof FatSeqUpdate) {
            onUpdateReceived(message);
        } else if (message instanceof ExecuteAfter) {
            onExecuteAfter((ExecuteAfter) message);
        } else if (message instanceof PushSeq) {
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
