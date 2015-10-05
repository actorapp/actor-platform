/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.updates;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.api.ApiDifferenceUpdate;
import im.actor.core.api.base.FatSeqUpdate;
import im.actor.core.api.base.SeqUpdate;
import im.actor.core.api.base.SeqUpdateTooLong;
import im.actor.core.api.base.WeakUpdate;
import im.actor.core.api.parser.UpdatesParser;
import im.actor.core.api.rpc.RequestGetDifference;
import im.actor.core.api.rpc.RequestGetState;
import im.actor.core.api.rpc.ResponseGetDifference;
import im.actor.core.api.rpc.ResponseSeq;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.updates.internal.ExecuteAfter;
import im.actor.core.modules.updates.internal.InternalUpdate;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.network.parser.Update;
import im.actor.runtime.Log;

public class SequenceActor extends ModuleActor {

    private static final String TAG = "Updates";
    private static final int INVALIDATE_GAP = 2000;// 2 Secs
    private static final int INVALIDATE_MAX_SEC_HOLE = 10;

    private static final String KEY_SEQ = "updates_seq";
    private static final String KEY_STATE = "updates_state";

    private HashMap<Integer, Object> further = new HashMap<Integer, Object>();

    private ArrayList<ExecuteAfter> pendingRunnables = new ArrayList<ExecuteAfter>();

    private boolean isValidated = true;
    private boolean isTimerStarted = false;

    private int seq;
    private byte[] state;

    private UpdateProcessor processor;
    private UpdatesParser parser;

    public SequenceActor(ModuleContext modules) {
        super(modules);
    }

    @Override
    public void preStart() {
        seq = preferences().getInt(KEY_SEQ, -1);
        state = preferences().getBytes(KEY_STATE);
        parser = new UpdatesParser();
        processor = new UpdateProcessor(context());

        self().send(new Invalidate());
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof Invalidate || message instanceof SeqUpdateTooLong ||
                message instanceof ForceInvalidate) {
            invalidate();
        } else if (message instanceof SeqUpdate) {
            onUpdateReceived(message);
        } else if (message instanceof FatSeqUpdate) {
            onUpdateReceived(message);
        } else if (message instanceof WeakUpdate) {
            onUpdateReceived(message);
        } else if (message instanceof InternalUpdate) {
            onUpdateReceived(message);
        } else if (message instanceof ExecuteAfter) {
            onUpdateReceived(message);
        } else if (message instanceof PushSeq) {
            onUpdateReceived(message);
        } else {
            drop(message);
        }
    }

    private void onUpdateReceived(Object u) {
        // Building parameters
        int seq;
        byte[] state;
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
        } else if (u instanceof WeakUpdate) {
            WeakUpdate w = (WeakUpdate) u;
            try {
                Update update = parser.read(w.getUpdateHeader(), w.getUpdate());
                processor.processWeakUpdate(update, w.getDate());
                Log.d(TAG, "Weak Update: " + update);
            } catch (IOException e) {
                e.printStackTrace();
                Log.w(TAG, "Unable to parse update: ignoring");
            }
            return;
        } else if (u instanceof InternalUpdate) {
            Log.d(TAG, "Received internal update");
            processor.processInternalUpdate((InternalUpdate) u);
            return;
        } else if (u instanceof ExecuteAfter) {
            ExecuteAfter after = (ExecuteAfter) u;
            if (after.getSeq() <= this.seq) {
                after.getRunnable().run();
            } else {
                pendingRunnables.add(after);
            }
            return;
        } else if (u instanceof PushSeq) {
            PushSeq pushSeq = (PushSeq) u;
            if (pushSeq.seq <= this.seq) {
                Log.d(TAG, "Ignored PushSeq {seq:" + pushSeq.seq + "}");
            } else {
                Log.w(TAG, "External Out of sequence: starting timer for invalidation");
                self().sendOnce(new ForceInvalidate(), INVALIDATE_GAP);
            }
            return;
        } else {
            return;
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

        if (!isValidSeq(seq)) {
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

        // Checking update
        Update update = null;
        try {
            update = new UpdatesParser().read(type, body);
        } catch (IOException e) {
            Log.w(TAG, "Unable to parse update: ignoring");
            e.printStackTrace();
        }

        if (update != null) {
            if ((!(u instanceof FatSeqUpdate)) && processor.isCausesInvalidation(update)) {
                Log.w(TAG, "Message causes invalidation");
                invalidate();
                return;
            }

            // Processing update
            Log.d(TAG, "Processing update: " + update);

            if (u instanceof FatSeqUpdate) {
                FatSeqUpdate fatSeqUpdate = (FatSeqUpdate) u;
                processor.applyRelated(fatSeqUpdate.getUsers(), fatSeqUpdate.getGroups(), false);
            }

            processor.processUpdate(update);

            if (u instanceof FatSeqUpdate) {
                FatSeqUpdate fatSeqUpdate = (FatSeqUpdate) u;
                processor.applyRelated(fatSeqUpdate.getUsers(), fatSeqUpdate.getGroups(), true);
            }
        }

        // Saving state
        this.seq = seq;
        this.state = state;
        preferences().putInt(KEY_SEQ, seq);
        preferences().putBytes(KEY_STATE, state);

        checkRunnables();
        checkFuture();

        // Faaaaaar away
        isTimerStarted = false;
        self().sendOnce(new ForceInvalidate(), 24 * 60 * 60 * 1000L);
    }

    private boolean isValidSeq(final int seq) {
        return this.seq <= 0 || seq == this.seq + 1;
    }

    private void invalidate() {
        if (!isValidated) {
            return;
        }
        isValidated = false;

        if (seq < 0) {
            Log.d(TAG, "Loading fresh state...");
            request(new RequestGetState(), new RpcCallback<ResponseSeq>() {
                @Override
                public void onResult(ResponseSeq response) {
                    if (isValidated) {
                        return;
                    }

                    seq = response.getSeq();
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
            request(new RequestGetDifference(seq, state), new RpcCallback<ResponseGetDifference>() {
                @Override
                public void onResult(ResponseGetDifference response) {
                    if (isValidated) {
                        return;
                    }

                    Log.d(TAG, "Difference loaded {seq=" + response.getSeq() + "} in "
                            + (im.actor.runtime.Runtime.getCurrentTime() - loadStart) + " ms");

                    long parseStart = im.actor.runtime.Runtime.getCurrentTime();
                    ArrayList<Update> updates = new ArrayList<Update>();
                    for (ApiDifferenceUpdate u : response.getUpdates()) {
                        try {
                            updates.add(parser.read(u.getUpdateHeader(), u.getUpdate()));
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, "Broken update #" + u.getUpdateHeader() + ": ignoring");
                        }
                    }
                    Log.d(TAG, "Difference parsed  in " + (im.actor.runtime.Runtime.getCurrentTime() - parseStart) + " ms");

                    long applyStart = im.actor.runtime.Runtime.getCurrentTime();
                    processor.applyDifferenceUpdate(response.getUsers(), response.getGroups(), updates);
                    Log.d(TAG, "Difference applied in " + (im.actor.runtime.Runtime.getCurrentTime() - applyStart) + " ms");

                    seq = response.getSeq();
                    state = response.getState();

                    isValidated = true;

                    preferences().putInt(KEY_SEQ, seq);
                    preferences().putBytes(KEY_STATE, state);

                    checkRunnables();
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

    private void onUpdateStarted() {
        context().getAppStateModule().getAppStateVM().getIsSyncing().change(true);
    }

    private void onUpdateEnded() {
        context().getAppStateModule().getAppStateVM().getIsSyncing().change(false);
    }

    private void checkFuture() {
        for (int i = seq + 1; ; i++) {
            if (further.containsKey(i)) {
                onReceive(further.remove(i));
            } else {
                break;
            }
        }
        further.clear();
    }

    private void checkRunnables() {
        if (pendingRunnables.size() > 0) {
            for (ExecuteAfter e : pendingRunnables.toArray(new ExecuteAfter[pendingRunnables.size()])) {
                if (e.getSeq() <= this.seq) {
                    e.getRunnable().run();
                    pendingRunnables.remove(e);
                }
            }
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
