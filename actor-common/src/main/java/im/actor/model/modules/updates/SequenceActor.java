package im.actor.model.modules.updates;

import im.actor.model.Messenger;
import im.actor.model.api.ContactRecord;
import im.actor.model.api.DifferenceUpdate;
import im.actor.model.api.Group;
import im.actor.model.api.User;
import im.actor.model.api.base.FatSeqUpdate;
import im.actor.model.api.base.SeqUpdate;
import im.actor.model.api.base.SeqUpdateTooLong;
import im.actor.model.api.base.WeakUpdate;
import im.actor.model.api.parser.UpdatesParser;
import im.actor.model.api.rpc.RequestGetDifference;
import im.actor.model.api.rpc.RequestGetState;
import im.actor.model.api.rpc.ResponseGetDifference;
import im.actor.model.api.rpc.ResponseSeq;
import im.actor.model.log.Log;
import im.actor.model.modules.Updates;
import im.actor.model.modules.utils.ModuleActor;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;
import im.actor.model.network.parser.Update;
import im.actor.model.storage.PreferencesStorage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class SequenceActor extends ModuleActor {

    private static final String TAG = "Updates";
    private static final int INVALIDATE_GAP = 2000;// 2 Secs

    private static final String KEY_SEQ = "updates_seq";
    private static final String KEY_STATE = "updates_state";

    private HashMap<Integer, Object> further = new HashMap<Integer, Object>();
    private PreferencesStorage preferencesStorage;

    private boolean isValidated = true;
    private int seq;
    private byte[] state;

    private Messenger messenger;
    private UpdateProcessor processor;
    private UpdatesParser parser;

    public SequenceActor(Updates updates) {
        super(updates.getMessenger());
        this.messenger = updates.getMessenger();
        this.preferencesStorage = updates.getMessenger().getConfiguration().getPreferencesStorage();
    }

    @Override
    public void preStart() {
        seq = preferencesStorage.getInt(KEY_SEQ, -1);
        state = preferencesStorage.getBytes(KEY_STATE);
        parser = new UpdatesParser();
        processor = new UpdateProcessor(messenger);

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
            Log.w(TAG, "Received weak update");
            try {
                processor.processUpdate(parser.read(w.getUpdateHeader(), w.getUpdate()));
            } catch (IOException e) {
                e.printStackTrace();
                Log.w(TAG, "Unable to parse update: ignoring");
            }
            return;
        } else {
            return;
        }

        // Checking sequence
        if (seq <= this.seq) {
            Log.d(TAG, "Ignored SeqUpdate {seq:" + seq + "}");
            return;
        }
        Log.d(TAG, "SeqUpdate {seq:" + seq + "}");

        if (!isValidated) {
            Log.d(TAG, "Caching in further map");
            further.put(seq, u);
            return;
        }

        if (!isValidSeq(seq)) {
            Log.w(TAG, "Out of sequence: starting timer for invalidation");
            further.put(seq, u);
            self().sendOnce(new ForceInvalidate(), INVALIDATE_GAP);
            return;
        }

        // Checking update
        Update update;
        try {
            update = new UpdatesParser().read(type, body);
        } catch (IOException e) {
            Log.w(TAG, "Unable to parse update: ignoring");
            e.printStackTrace();
            return;
        }

        if (processor.isCausesInvalidation(update)) {
            Log.w(TAG, "Message causes invalidation");
            invalidate();
            return;
        }

        // Processing update
        Log.d(TAG, "Processing update: " + update);

        if (u instanceof FatSeqUpdate) {
            FatSeqUpdate fatSeqUpdate = (FatSeqUpdate) u;
            processor.applyRelated(fatSeqUpdate.getUsers(), fatSeqUpdate.getGroups(), fatSeqUpdate.getContacts(), false);
        }

        processor.processUpdate(update);

        if (u instanceof FatSeqUpdate) {
            FatSeqUpdate fatSeqUpdate = (FatSeqUpdate) u;
            processor.applyRelated(fatSeqUpdate.getUsers(), fatSeqUpdate.getGroups(), fatSeqUpdate.getContacts(), true);
        }

        // Saving state
        this.seq = seq;
        this.state = state;
        preferencesStorage.putInt(KEY_SEQ, seq);
        preferencesStorage.putBytes(KEY_STATE, state);

        checkFuture();

        // Faaaaaar away
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

                    preferencesStorage.putInt(KEY_SEQ, seq);
                    preferencesStorage.putBytes(KEY_STATE, state);

                    Log.d(TAG, "State loaded {seq=" + seq + "}");

                    // Faaaaaar away
                    self().sendOnce(new ForceInvalidate(), 24 * 60 * 60 * 1000L);

                    checkFuture();
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
            request(new RequestGetDifference(seq, state), new RpcCallback<ResponseGetDifference>() {
                @Override
                public void onResult(ResponseGetDifference response) {
                    if (isValidated) {
                        return;
                    }

                    Log.d(TAG, "Difference loaded {seq=" + response.getSeq() + "}");

                    processor.applyRelated(response.getUsers(), response.getGroups(), response.getContacts(), false);
                    for (DifferenceUpdate u : response.getUpdates()) {
                        try {
                            Update update = parser.read(u.getUpdateHeader(), u.getUpdate());
                            processor.processUpdate(update);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, "Broken update #" + u.getUpdateHeader() + ": ignoring");
                        }
                    }
                    processor.applyRelated(response.getUsers(), response.getGroups(), response.getContacts(), true);

                    seq = response.getSeq();
                    state = response.getState();

                    isValidated = true;

                    preferencesStorage.putInt(KEY_SEQ, seq);
                    preferencesStorage.putBytes(KEY_STATE, state);

                    // Faaaaaar away
                    self().sendOnce(new ForceInvalidate(), 24 * 60 * 60 * 1000L);

                    checkFuture();

                    if (response.needMore()) {
                        invalidate();
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


    // TODO: Check method
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
