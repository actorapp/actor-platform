package im.actor.core.modules.sequence;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiGroupOutPeer;
import im.actor.core.api.ApiOutPeer;
import im.actor.core.api.ApiUpdateContainer;
import im.actor.core.api.ApiUser;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.parser.UpdatesParser;
import im.actor.core.api.rpc.RequestGetReferencedEntitites;
import im.actor.core.api.rpc.RequestLoadEphermalPublicKeys;
import im.actor.core.api.rpc.ResponseGetDifference;
import im.actor.core.api.rpc.ResponseGetReferencedEntitites;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.updates.UpdateProcessor;
import im.actor.core.modules.updates.internal.InternalUpdate;
import im.actor.core.modules.updates.internal.LoggedIn;
import im.actor.core.modules.updates.internal.RelatedResponse;
import im.actor.core.util.ModuleActor;
import im.actor.core.network.parser.Update;
import im.actor.runtime.Log;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.function.Constructor;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.PromiseResolver;
import im.actor.runtime.promise.Promises;

public class SequenceHandlerActor extends ModuleActor {

    public static Constructor<SequenceHandlerActor> CONSTRUCTOR(final ModuleContext context) {
        return new Constructor<SequenceHandlerActor>() {
            @Override
            public SequenceHandlerActor create() {
                return new SequenceHandlerActor(context);
            }
        };
    }

    private static final String TAG = "SequenceHandlerActor";

    private UpdateProcessor processor;
    private UpdatesParser updatesParser;
    private boolean isUpdating;

    public SequenceHandlerActor(ModuleContext context) {
        super(context);

        this.processor = new UpdateProcessor(context);
    }

    @Override
    public void preStart() {
        super.preStart();
        this.updatesParser = new UpdatesParser();
    }

    private void onInternalUpdate(InternalUpdate internalUpdate) {
        processor.processInternalUpdate(internalUpdate);
    }

    private void onWeakUpdateReceived(int type, byte[] body, long date) {
        Update update;
        try {
            update = updatesParser.read(type, body);
        } catch (IOException e) {
            Log.w(TAG, "Unable to parse update: ignoring");
            Log.e(TAG, e);
            return;
        }

        Log.d(TAG, "Processing weak update: " + update);
        this.processor.processWeakUpdate(update, date);
    }

    private void onRelatedResponse(List<ApiUser> relatedUsers, List<ApiGroup> relatedGroups, Runnable afterApply) {
        processor.applyRelated(relatedUsers, relatedGroups, false);
        afterApply.run();
    }

    private Promise<UpdateProcessed> onSeqUpdate(int type, byte[] body,
                                                 @Nullable List<ApiUser> users,
                                                 @Nullable List<ApiGroup> groups) throws Exception {

        Update update;
        try {
            update = updatesParser.read(type, body);
        } catch (IOException e) {
            Log.w(TAG, "Unable to parse update: ignoring");
            Log.e(TAG, e);
            return Promises.success(new UpdateProcessed());
        }

        if (groups == null || users == null) {
            if (processor.isCausesInvalidation(update)) {
                Log.w(TAG, "Difference is required");
                throw new RuntimeException("Difference is required");
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
        return Promises.success(new UpdateProcessed());
    }

    private Promise<UpdateProcessed> onDifferenceUpdate(final ResponseGetDifference difference) {
        long parseStart = im.actor.runtime.Runtime.getCurrentTime();
        final ArrayList<Update> updates = new ArrayList<Update>();
        for (ApiUpdateContainer u : difference.getUpdates()) {
            try {
                updates.add(updatesParser.read(u.getUpdateHeader(), u.getUpdate()));
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Broken update #" + u.getUpdateHeader() + ": ignoring");
            }
        }
        Log.d(TAG, "Difference parsed  in " + (im.actor.runtime.Runtime.getCurrentTime() - parseStart) + " ms");

        if (updates.size() > 0) {
            String command = "Difference updates:";
            for (Update u : updates) {
                command += "\n| " + u;
            }
            Log.d(TAG, command);
        }

        final ArrayList<ApiUserOutPeer> pendingUserPeers = new ArrayList<>();
        final ArrayList<ApiGroupOutPeer> pendingGroupPeers = new ArrayList<>();
        for (ApiUserOutPeer refPeer : difference.getUsersRefs()) {
            if (getUser(refPeer.getUid()) != null) {
                continue;
            }
            pendingUserPeers.add(refPeer);
        }
        for (ApiGroupOutPeer refPeer : difference.getGroupsRefs()) {
            if (getGroup(refPeer.getGroupId()) != null) {
                continue;
            }
            pendingGroupPeers.add(refPeer);
        }

        if (pendingGroupPeers.size() > 0 || pendingUserPeers.size() > 0) {
            Log.d(TAG, "Downloading pending peers (users: " + pendingUserPeers.size() + ", groups: " + pendingGroupPeers.size() + ")");
            isUpdating = true;
            return new Promise<>(new PromiseFunc<UpdateProcessed>() {
                @Override
                public void exec(final PromiseResolver<UpdateProcessed> resolver) {
                    api(new RequestGetReferencedEntitites(pendingUserPeers, pendingGroupPeers))
                            .then(new Consumer<ResponseGetReferencedEntitites>() {
                                @Override
                                public void apply(ResponseGetReferencedEntitites responseGetReferencedEntitites) {
                                    Log.d(TAG, "Pending peers downloaded");
                                    processor.applyRelated(responseGetReferencedEntitites.getUsers(),
                                            responseGetReferencedEntitites.getGroups(), false);
                                    long applyStart = im.actor.runtime.Runtime.getCurrentTime();
                                    processor.applyDifferenceUpdate(difference.getUsers(), difference.getGroups(), updates);
                                    Log.d(TAG, "Difference applied in " + (im.actor.runtime.Runtime.getCurrentTime() - applyStart) + " ms");
                                    resolver.result(new UpdateProcessed());
                                    unstashAll();
                                    isUpdating = false;
                                }
                            })
                            .failure(new Consumer<Exception>() {
                                @Override
                                public void apply(Exception e) {
                                    resolver.error(e);
                                    unstashAll();
                                    isUpdating = false;
                                }
                            }).done(self());
                }
            });
        } else {
            long applyStart = im.actor.runtime.Runtime.getCurrentTime();
            processor.applyDifferenceUpdate(difference.getUsers(), difference.getGroups(), updates);
            Log.d(TAG, "Difference applied in " + (im.actor.runtime.Runtime.getCurrentTime() - applyStart) + " ms");
            return Promises.success(new UpdateProcessed());
        }
    }

    @Override
    public void onReceive(Object message) {
        Log.d(TAG, "Processing onReceive: " + message);
        if (message instanceof WeakUpdate) {
            WeakUpdate weakUpdate = (WeakUpdate) message;
            try {
                onWeakUpdateReceived(weakUpdate.type, weakUpdate.body, weakUpdate.date);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (message instanceof RelatedResponse) {
            if (isUpdating) {
                stash();
                return;
            }
            onRelatedResponse(((RelatedResponse) message).getRelatedUsers(), ((RelatedResponse) message).getRelatedGroups(),
                    ((RelatedResponse) message).getAfterApply());
        } else if (message instanceof InternalUpdate) {
            if (isUpdating) {
                stash();
                return;
            }
            onInternalUpdate((InternalUpdate) message);
        } else {
            super.onReceive(message);
        }
    }

    @Override
    public Promise onAsk(Object message) throws Exception {
        if (message instanceof SeqUpdate) {
            SeqUpdate seqUpdate = (SeqUpdate) message;
            if (isUpdating) {
                stash();
                return null;
            }
            return onSeqUpdate(seqUpdate.type, seqUpdate.body, seqUpdate.users, seqUpdate.groups);
        } else if (message instanceof DifferenceUpdate) {
            DifferenceUpdate differenceUpdate = (DifferenceUpdate) message;
            if (isUpdating) {
                stash();
                return null;
            }
            return onDifferenceUpdate(differenceUpdate.getDifference());
        } else {
            return super.onAsk(message);
        }
    }

    public static class WeakUpdate {

        private int type;
        private byte[] body;
        private long date;

        public WeakUpdate(int type, byte[] body, long date) {
            this.type = type;
            this.body = body;
            this.date = date;
        }

        public int getType() {
            return type;
        }

        public byte[] getBody() {
            return body;
        }

        public long getDate() {
            return date;
        }
    }

    public static class SeqUpdate implements AskMessage<UpdateProcessed> {

        private int type;
        private byte[] body;
        @Nullable
        private List<ApiUser> users;
        @Nullable
        private
        List<ApiGroup> groups;

        public SeqUpdate(int type, byte[] body, @Nullable List<ApiUser> users, @Nullable List<ApiGroup> groups) {
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

        public
        @Nullable
        List<ApiUser> getUsers() {
            return users;
        }

        public
        @Nullable
        List<ApiGroup> getGroups() {
            return groups;
        }
    }

    public static class DifferenceUpdate implements AskMessage<UpdateProcessed> {
        private ResponseGetDifference difference;

        public DifferenceUpdate(ResponseGetDifference difference) {
            this.difference = difference;
        }

        public ResponseGetDifference getDifference() {
            return difference;
        }
    }

    public static class UpdateProcessed {

    }
}
