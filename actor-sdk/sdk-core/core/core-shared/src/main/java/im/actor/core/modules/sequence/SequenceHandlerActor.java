package im.actor.core.modules.sequence;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiGroupOutPeer;
import im.actor.core.api.ApiUser;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.rpc.RequestGetReferencedEntitites;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.sequence.internal.HandlerDifferenceUpdates;
import im.actor.core.modules.sequence.internal.HandlerSeqUpdate;
import im.actor.core.modules.sequence.internal.HandlerWeakUpdate;
import im.actor.core.modules.sequence.internal.HandlerRelatedResponse;
import im.actor.core.modules.ModuleActor;
import im.actor.core.modules.sequence.processor.UpdateProcessor;
import im.actor.core.network.parser.Update;
import im.actor.runtime.Log;
import im.actor.runtime.Runtime;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.function.Constructor;
import im.actor.runtime.function.Function;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.promise.PromisesArray;

/*-[
#pragma clang diagnostic ignored "-Wnullability-completeness"
]-*/

public class SequenceHandlerActor extends ModuleActor {

    public static ActorCreator CONSTRUCTOR(final ModuleContext context) {
        return () -> new SequenceHandlerActor(context);
    }

    private static final String TAG = "SequenceHandlerActor";

    // Do Not Remove! WorkAround for missing j2objc translator include
    private static final Void DUMB = null;

    private final UpdateProcessor processor;

    private boolean isUpdating;

    public SequenceHandlerActor(ModuleContext context) {
        super(context);

        this.processor = new UpdateProcessor(context);
    }


    //
    // Sequenced data
    //

    private Promise<Void> onSeqUpdate(final Update update,
                                      @Nullable List<ApiUser> users,
                                      @Nullable List<ApiGroup> groups) throws Exception {

        Log.d(TAG, "Processing update: " + update);

        beginUpdates();

        // Related Users
        Promise<Void> currentPromise;
        if (groups != null && users != null && (users.size() > 0 || groups.size() > 0)) {
            currentPromise = updates().applyRelatedData(users, groups);
        } else {
            currentPromise = Promise.success(null);
        }

        // Update Application
        currentPromise = currentPromise
                .chain(v -> processor.processUpdate(update));

        // Handling update end
        currentPromise.then(v -> endUpdates());

        // TODO: Wait database flush

        return currentPromise;
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

        beginUpdates();

        // Related Users
        Promise<Void> currentPromise = updates().applyRelatedData(users, groups);

        // Loading missing peers
        currentPromise = currentPromise.chain(v -> updates().loadRequiredPeers(userOutPeers, groupOutPeers));

        // Apply Diff
        long applyStart = im.actor.runtime.Runtime.getCurrentTime();
        currentPromise = currentPromise
                .chain(v -> processor.applyDifferenceUpdate(updates))
                .then(v -> {
                    Log.d(TAG, "Difference applied in " + (im.actor.runtime.Runtime.getCurrentTime() - applyStart) + " ms");
                    endUpdates();
                });

        // TODO: Wait database flush

        return currentPromise;
    }


    //
    // Weak Updates
    //

    private void onWeakUpdateReceived(Update update, long date) {
        Log.d(TAG, "Processing weak update: " + update);
        this.processor.processWeakUpdate(update, date);
    }


    //
    // Tools
    //

    private void beginUpdates() {
        isUpdating = true;
    }

    private void endUpdates() {
        isUpdating = false;
        unstashAll();
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
