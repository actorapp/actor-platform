package im.actor.core.modules.sequence;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiGroupOutPeer;
import im.actor.core.api.ApiMessageContainer;
import im.actor.core.api.ApiUpdateContainer;
import im.actor.core.api.ApiUser;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.parser.UpdatesParser;
import im.actor.core.api.rpc.RequestGetReferencedEntitites;
import im.actor.core.api.rpc.ResponseGetDifference;
import im.actor.core.api.rpc.ResponseGetReferencedEntitites;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.sequence.internal.HandlerDifferenceUpdates;
import im.actor.core.modules.sequence.internal.HandlerSeqUpdate;
import im.actor.core.modules.sequence.internal.HandlerWeakUpdate;
import im.actor.core.modules.sequence.internal.InternalUpdate;
import im.actor.core.modules.sequence.internal.RelatedResponse;
import im.actor.core.modules.ModuleActor;
import im.actor.core.network.parser.Update;
import im.actor.runtime.Log;
import im.actor.runtime.Runtime;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.function.Constructor;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.PromiseResolver;
import im.actor.runtime.promise.Promises;

/*-[
#pragma clang diagnostic ignored "-Wnullability-completeness"
]-*/

public class SequenceHandlerActor extends ModuleActor {

    public static Constructor<SequenceHandlerActor> CONSTRUCTOR(final ModuleContext context) {
        return () -> new SequenceHandlerActor(context);
    }

    private static final String TAG = "SequenceHandlerActor";

    private UpdateProcessor processor;
    private boolean isUpdating;

    public SequenceHandlerActor(ModuleContext context) {
        super(context);

        this.processor = new UpdateProcessor(context);
    }

    private void onInternalUpdate(InternalUpdate internalUpdate) {
        processor.processInternalUpdate(internalUpdate);
    }

    private void onWeakUpdateReceived(Update update, long date) {
        Log.d(TAG, "Processing weak update: " + update);
        this.processor.processWeakUpdate(update, date);
    }

    private void onRelatedResponse(List<ApiUser> relatedUsers, List<ApiGroup> relatedGroups, Runnable afterApply) {
        processor.applyRelated(relatedUsers, relatedGroups, false);
        afterApply.run();
    }

    private Promise<Void> onSeqUpdate(Update update,
                                      @Nullable List<ApiUser> users,
                                      @Nullable List<ApiGroup> groups) throws Exception {

        // Processing update
        Log.d(TAG, "Processing update: " + update);

        if (groups != null && users != null) {
            processor.applyRelated(users, groups, false);
        }

        processor.processUpdate(update);

        if (groups != null && users != null) {
            processor.applyRelated(users, groups, true);
        }

        // Log.d(TAG, "Processing update success");
        return Promise.success(Void.INSTANCE);
    }

    private Promise<Void> onDifferenceUpdate(@NotNull List<ApiUser> users,
                                             @NotNull List<ApiGroup> groups,
                                             @NotNull List<ApiUserOutPeer> userOutPeers,
                                             @NotNull List<ApiGroupOutPeer> groupOutPeers,
                                             @NotNull List<Update> updates) {

        if (updates.size() > 0) {
            String command = "Difference updates:";
            for (Update u : updates) {
                command += "\n| " + u;
            }
            Log.d(TAG, command);
        }

        final ArrayList<ApiUserOutPeer> pendingUserPeers = new ArrayList<>();
        final ArrayList<ApiGroupOutPeer> pendingGroupPeers = new ArrayList<>();
        for (ApiUserOutPeer refPeer : userOutPeers) {
            if (getUser(refPeer.getUid()) != null) {
                continue;
            }
            pendingUserPeers.add(refPeer);
        }
        for (ApiGroupOutPeer refPeer : groupOutPeers) {
            if (getGroup(refPeer.getGroupId()) != null) {
                continue;
            }
            pendingGroupPeers.add(refPeer);
        }

        if (pendingGroupPeers.size() > 0 || pendingUserPeers.size() > 0) {
            Log.d(TAG, "Downloading pending peers (users: " + pendingUserPeers.size() + ", groups: " + pendingGroupPeers.size() + ")");
            isUpdating = true;
            return new Promise<>((PromiseFunc<Void>) resolver ->
                    api(new RequestGetReferencedEntitites(pendingUserPeers, pendingGroupPeers))
                            .then(responseGetReferencedEntitites -> {
                                Log.d(TAG, "Pending peers downloaded");
                                processor.applyRelated(responseGetReferencedEntitites.getUsers(),
                                        responseGetReferencedEntitites.getGroups(), false);
                                long applyStart = Runtime.getCurrentTime();
                                processor.applyDifferenceUpdate(users, groups, updates);
                                Log.d(TAG, "Difference applied in " + (Runtime.getCurrentTime() - applyStart) + " ms");
                                resolver.result(Void.INSTANCE);
                                unstashAll();
                                isUpdating = false;
                            }));
        } else {
            long applyStart = im.actor.runtime.Runtime.getCurrentTime();
            processor.applyDifferenceUpdate(users, groups, updates);
            Log.d(TAG, "Difference applied in " + (im.actor.runtime.Runtime.getCurrentTime() - applyStart) + " ms");
            return Promise.success(Void.INSTANCE);
        }
    }


    //
    // Message Processing
    //

    @Override
    public void onReceive(Object message) {
        if (message instanceof HandlerWeakUpdate) {
            HandlerWeakUpdate weakUpdate = (HandlerWeakUpdate) message;
            try {
                onWeakUpdateReceived(weakUpdate.getUpdate(), weakUpdate.getDate());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (message instanceof RelatedResponse) {
            if (isUpdating) {
                stash();
                return;
            }
            RelatedResponse relatedResponse = (RelatedResponse) message;
            onRelatedResponse(
                    relatedResponse.getRelatedUsers(),
                    relatedResponse.getRelatedGroups(),
                    relatedResponse.getAfterApply());

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
        if (message instanceof HandlerSeqUpdate) {
            HandlerSeqUpdate seqUpdate = (HandlerSeqUpdate) message;
            if (isUpdating) {
                stash();
                return null;
            }
            return onSeqUpdate(seqUpdate.getUpdate(), seqUpdate.getUsers(), seqUpdate.getGroups());
        } else if (message instanceof HandlerDifferenceUpdates) {
            HandlerDifferenceUpdates differenceUpdate = (HandlerDifferenceUpdates) message;
            if (isUpdating) {
                stash();
                return null;
            }
            return onDifferenceUpdate(
                    differenceUpdate.getUsers(),
                    differenceUpdate.getGroups(),
                    differenceUpdate.getUserOutPeers(),
                    differenceUpdate.getGroupOutPeers(),
                    differenceUpdate.getUpdates());
        } else {
            return super.onAsk(message);
        }
    }
}
