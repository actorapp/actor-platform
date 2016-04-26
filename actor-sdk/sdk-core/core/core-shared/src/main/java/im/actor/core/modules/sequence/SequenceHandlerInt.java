package im.actor.core.modules.sequence;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiGroupOutPeer;
import im.actor.core.api.ApiUser;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.modules.sequence.internal.HandlerDifferenceUpdates;
import im.actor.core.modules.sequence.internal.HandlerSeqUpdate;
import im.actor.core.modules.sequence.internal.HandlerWeakUpdate;
import im.actor.core.modules.sequence.internal.HandlerRelatedResponse;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

public class SequenceHandlerInt extends ActorInterface {

    public SequenceHandlerInt(ActorRef dest) {
        super(dest);
    }

    public Promise<Void> onSeqUpdate(Update update,
                                     @Nullable List<ApiUser> users,
                                     @Nullable List<ApiGroup> groups) {
        return ask(new HandlerSeqUpdate(update, users, groups));
    }

    public Promise<Void> onDifferenceUpdate(@NotNull List<ApiUser> users,
                                            @NotNull List<ApiGroup> groups,
                                            @NotNull List<ApiUserOutPeer> userOutPeers,
                                            @NotNull List<ApiGroupOutPeer> groupOutPeers,
                                            @NotNull List<Update> updates) {
        return ask(new HandlerDifferenceUpdates(users, groups, userOutPeers, groupOutPeers, updates));
    }

    public Promise<Void> onRelatedResponse(@NotNull List<ApiUser> users,
                                           @NotNull List<ApiGroup> groups) {
        return ask(new HandlerRelatedResponse(users, groups));
    }

    public void onWeakUpdate(Update update, long date) {
        send(new HandlerWeakUpdate(update, date));
    }
}
