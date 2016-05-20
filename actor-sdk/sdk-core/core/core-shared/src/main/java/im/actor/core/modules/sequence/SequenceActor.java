/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.sequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiUpdateContainer;
import im.actor.core.api.ApiUser;
import im.actor.core.api.base.FatSeqUpdate;
import im.actor.core.api.base.SeqUpdate;
import im.actor.core.api.base.SeqUpdateTooLong;
import im.actor.core.api.base.WeakUpdate;
import im.actor.core.api.parser.UpdatesParser;
import im.actor.core.api.rpc.RequestGetDifference;
import im.actor.core.api.rpc.RequestGetState;
import im.actor.core.modules.api.ApiSupportConfiguration;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.sequence.internal.ExecuteAfter;
import im.actor.core.modules.ModuleActor;
import im.actor.core.modules.sequence.processor.UpdateValidator;
import im.actor.core.network.parser.Update;
import im.actor.runtime.*;
import im.actor.runtime.Runtime;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.Cancellable;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.power.WakeLock;

public class SequenceActor extends ModuleActor {

    public static ActorCreator CONSTRUCTOR(final ModuleContext context) {
        return () -> new SequenceActor(context);
    }

    // Do Not Remove! WorkAround for missing j2objc translator include
    private static final Void DUMB = null;

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

    private Cancellable forceInvalidateCancellable;

    private UpdateValidator validator;

    private UpdatesParser updatesParser;

    private SequenceHandlerInt handler;

    private WakeLock currentWakeLock;

    public SequenceActor(ModuleContext modules) {
        super(modules);
    }

    @Override
    public void preStart() {
        seq = preferences().getInt(KEY_SEQ, -1);
        state = preferences().getBytes(KEY_STATE);
        finishedSeq = seq;
        finishedState = state;

        validator = new UpdateValidator(context());
        updatesParser = new UpdatesParser();

        handler = context().getUpdatesModule().getUpdateHandler();

        currentWakeLock = im.actor.runtime.Runtime.makeWakeLock();

        self().send(new Invalidate());
    }

    private void onWeakUpdateReceived(int type, byte[] body, long date) {
        Update update;
        try {
            update = updatesParser.read(type, body);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        handler.onWeakUpdate(update, date);
    }

    private void onPushSeqReceived(int seq) {
        if (seq <= this.seq) {
            Log.d(TAG, "Ignored PushSeq {seq:" + seq + "}");
        } else {
            Log.w(TAG, "External Out of sequence: starting timer for invalidation");
            startInvalidationTimer();
        }
    }

    @Deprecated
    private void onExecuteAfter(ExecuteAfter after) {
        if (after.getSeq() <= this.finishedSeq) {
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

        Update update = null;
        try {
            update = updatesParser.read(type, body);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (update != null) {

            if ((users == null || users.size() == 0) && (groups == null || groups.size() == 0)) {
                if (validator.isCausesInvalidation(update)) {
                    Log.w(TAG, "Invalidating: No Users of Groups available for update");
                    forceInvalidate();
                    return;
                }
            }


            Log.d(TAG, "Handling update #" + seq);

            startWakeLock();

            handler.onSeqUpdate(update, users, groups).then(aVoid -> {
                Log.d(TAG, "Handling update ended #" + seq);
                onUpdatesApplied(seq, state);
            });
        }

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

        startWakeLock();

        if (seq < 0) {
            Log.d(TAG, "Loading fresh state...");
            api(new RequestGetState(ApiSupportConfiguration.OPTIMIZATIONS)).then(response -> {
                if (isValidated) {
                    return;
                }

                Log.d(TAG, "State loaded {seq=" + seq + "}");

                seq = response.getSeq();
                state = response.getState();
                persistState(seq, state);

                stopWakeLock();

                onBecomeValid(response.getSeq(), response.getState());
            });
        } else {
            Log.d(TAG, "Loading difference...");
            onUpdateStarted();
            final long loadStart = im.actor.runtime.Runtime.getCurrentTime();
            api(new RequestGetDifference(seq, state, ApiSupportConfiguration.OPTIMIZATIONS)).then(response -> {

                if (isValidated) {
                    return;
                }

                long parsingStart = Runtime.getCurrentTime();
                ArrayList<Update> updates = new ArrayList<>();
                for (ApiUpdateContainer container : response.getUpdates()) {
                    try {
                        updates.add(updatesParser.read(container.getUpdateHeader(), container.getUpdate()));
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Ignore
                    }
                }

                Log.d(TAG, "Difference loaded {seq=" + response.getSeq() + "} in "
                        + (Runtime.getCurrentTime() - loadStart) + " ms, " +
                        "parsed in " + (Runtime.getCurrentTime() - parsingStart) + " ms, " +
                        "userRefs: " + response.getUsersRefs().size() + ", " +
                        "groupRefs: " + response.getGroupsRefs().size());

                handler.onDifferenceUpdate(
                        response.getUsers(), response.getGroups(), response.getUsersRefs(),
                        response.getGroupsRefs(), updates).then(updateProcessed ->
                        onUpdatesApplied(response.getSeq(), response.getState())
                );

                onBecomeValid(response.getSeq(), response.getState());

                if (response.needMore()) {
                    invalidate();
                } else {
                    onUpdateEnded();
                }
            });
        }
    }

    private void onUpdatesApplied(int seq, byte[] state) {
        if (seq > finishedSeq) {
            persistState(seq, state);
            if (this.seq == seq) {
                Log.d(TAG, "All updates applied {seq:" + seq + "}");
                stopWakeLock();
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
        context().getAppStateModule().getGlobalStateVM().getIsSyncing().change(true);
    }

    private void onUpdateEnded() {
        context().getAppStateModule().getGlobalStateVM().getIsSyncing().change(false);
    }

    //
    // Invalidation Timer
    //

    private void stopInvalidationTimer() {
        isTimerStarted = false;
        if (forceInvalidateCancellable != null) {
            forceInvalidateCancellable.cancel();
            forceInvalidateCancellable = null;
        }
    }

    private void startInvalidationTimer() {
        if (!isTimerStarted) {
            if (forceInvalidateCancellable != null) {
                forceInvalidateCancellable.cancel();
                forceInvalidateCancellable = null;
            }
            forceInvalidateCancellable = schedule(new ForceInvalidate(), INVALIDATE_GAP);
            isTimerStarted = true;
        }
    }

    private void forceInvalidate() {
        if (forceInvalidateCancellable != null) {
            forceInvalidateCancellable.cancel();
            forceInvalidateCancellable = null;
        }
        forceInvalidateCancellable = schedule(new ForceInvalidate(), 0);
        isTimerStarted = false;
    }


    //
    // Weak Locks
    //

    private void startWakeLock() {
        if (currentWakeLock == null) {
            currentWakeLock = Runtime.makeWakeLock();
            Log.w(TAG, "Starting Wake Lock");
        }
    }

    private void stopWakeLock() {
        if (currentWakeLock != null) {
            currentWakeLock.releaseLock();
            currentWakeLock = null;
            Log.w(TAG, "Released Wake Lock");
        }
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
        } else if (message instanceof WeakUpdate) {
            WeakUpdate weakUpdate = (WeakUpdate) message;
            onWeakUpdateReceived(weakUpdate.getUpdateHeader(), weakUpdate.getUpdate(),
                    weakUpdate.getDate());
        } else {
            super.onReceive(message);
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
