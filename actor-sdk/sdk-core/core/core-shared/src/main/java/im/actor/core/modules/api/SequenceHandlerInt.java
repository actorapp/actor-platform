package im.actor.core.modules.api;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiUser;
import im.actor.core.api.rpc.ResponseGetDifference;
import im.actor.core.modules.updates.internal.InternalUpdate;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.promise.Promise;
import im.actor.core.modules.api.SequenceHandlerActor.*;

public class SequenceHandlerInt extends ActorInterface {

    public SequenceHandlerInt(ActorRef dest) {
        super(dest);
    }

    public Promise<UpdateProcessed> onSeqUpdate(int updateKey, byte[] data,
                                                @Nullable List<ApiUser> users,
                                                @Nullable List<ApiGroup> groups) {
        return ask(new SeqUpdate(updateKey, data, users, groups));
    }

    public Promise<UpdateProcessed> onDifferenceUpdate(ResponseGetDifference difference) {
        return ask(new DifferenceUpdate(difference));
    }

    public void onWeakUpdate(int type, byte[] data, long date) {
        send(new WeakUpdate(type, data, date));
    }

    public void onInternalUpdate(InternalUpdate internalUpdate) {
        send(internalUpdate);
    }
}
