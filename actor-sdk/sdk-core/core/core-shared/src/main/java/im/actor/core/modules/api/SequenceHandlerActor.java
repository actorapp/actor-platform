package im.actor.core.modules.api;

import java.io.IOException;
import java.util.List;

import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiUser;
import im.actor.core.api.parser.UpdatesParser;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.updates.UpdateProcessor;
import im.actor.core.util.ModuleActor;
import im.actor.core.network.parser.Update;
import im.actor.runtime.Log;
import im.actor.runtime.actors.future.Future;
import im.actor.runtime.actors.promise.PromiseExecutor;

public class SequenceHandlerActor extends ModuleActor {

    private static final String TAG = "SequenceHandlerActor";

    private UpdateProcessor processor;

    public SequenceHandlerActor(UpdateProcessor processor, ModuleContext context) {
        super(context);

        this.processor = processor;
    }

    private void onWeakUpdateReceived(Update update, long date) {
        Log.d(TAG, "Processing weak update: " + update);
        this.processor.processWeakUpdate(update, date);
    }

    private void onSeqUpdate(int type, byte[] body, List<ApiUser> users,
                             List<ApiGroup> groups, PromiseExecutor future) {

        Update update;
        try {
            update = new UpdatesParser().read(type, body);
        } catch (IOException e) {
            Log.w(TAG, "Unable to parse update: ignoring");
            Log.e(TAG, e);
            future.result(null);
            return;
        }

        if (groups == null || users == null) {
            if (processor.isCausesInvalidation(update)) {
                Log.w(TAG, "Difference is required");
                future.error(new RuntimeException("Difference is required"));
                return;
            }
        }

        // Processing update
        Log.d(TAG, "Processing update: " + update);

        if (groups != null && users != null) {
            processor.applyRelated(users, groups, false);
        }

        processor.processUpdate(update);

        if (groups != null && users != null) {
            processor.applyRelated(users, groups, true);
        }

        Log.d(TAG, "Processing update success");
        future.result(null);
    }

    @Override
    public void onAsk(Object message, PromiseExecutor future) {
        if (message instanceof WeakUpdate) {
            WeakUpdate weakUpdate = (WeakUpdate) message;
            onWeakUpdateReceived(weakUpdate.getUpdate(), weakUpdate.getDate());
            future.result(null);
        } else if (message instanceof SeqUpdate) {
            SeqUpdate seqUpdate = (SeqUpdate) message;
            onSeqUpdate(seqUpdate.type, seqUpdate.body,
                    seqUpdate.users, seqUpdate.groups, future);
        } else {
            super.onAsk(message, future);
        }
    }

    public static class WeakUpdate {

        private Update update;
        private long date;

        public WeakUpdate(Update update, long date) {
            this.update = update;
            this.date = date;
        }

        public long getDate() {
            return date;
        }

        public Update getUpdate() {
            return update;
        }
    }

    public static class SeqUpdate {

        private int type;
        private byte[] body;
        private List<ApiUser> users;
        private List<ApiGroup> groups;

        public SeqUpdate(int type, byte[] body, List<ApiUser> users, List<ApiGroup> groups) {
            this.type = type;
            this.body = body;
            this.users = users;
            this.groups = groups;
        }

        public int getType() {
            return type;
        }

        public byte[] getBody() {
            return body;
        }

        public List<ApiUser> getUsers() {
            return users;
        }

        public List<ApiGroup> getGroups() {
            return groups;
        }
    }
}
