package im.actor.core.modules.blocklist;

import java.util.List;

import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.rpc.RequestBlockUser;
import im.actor.core.api.rpc.RequestLoadBlockedUsers;
import im.actor.core.api.rpc.RequestUnblockUser;
import im.actor.core.api.updates.UpdateUserBlocked;
import im.actor.core.api.updates.UpdateUserUnblocked;
import im.actor.core.entity.Peer;
import im.actor.core.entity.User;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromisesArray;

public class BlockListModule extends AbsModule {

    public BlockListModule(final ModuleContext context) {
        super(context);
    }

    public Promise<List<User>> loadBlockedUsers() {
        return api(new RequestLoadBlockedUsers())
                .chain(response -> loadRequiredPeers(response.getUserPeers()))
                .flatMap(responseLoadBlockedUsers ->
                        PromisesArray.of(responseLoadBlockedUsers.getUserPeers())
                                .map(apiUserOutPeer -> users().getValueAsync(apiUserOutPeer.getUid()))
                                .zip());
    }

    public Promise<Void> blockUser(final int uid) {
        return buildOutPeer(Peer.user(uid))
                .flatMap(apiOutPeer ->
                        api(new RequestBlockUser(new ApiUserOutPeer(apiOutPeer.getId(), apiOutPeer.getAccessHash()))))
                .flatMap(responseSeq ->
                        updates().applyUpdate(
                                responseSeq.getSeq(),
                                responseSeq.getState(),
                                new UpdateUserBlocked(uid)));
    }

    public Promise<Void> unblockUser(final int uid) {
        return buildOutPeer(Peer.user(uid))
                .flatMap(apiOutPeer ->
                        api(new RequestUnblockUser(new ApiUserOutPeer(apiOutPeer.getId(), apiOutPeer.getAccessHash()))))
                .flatMap(responseSeq ->
                        updates().applyUpdate(
                                responseSeq.getSeq(),
                                responseSeq.getState(),
                                new UpdateUserUnblocked(uid)));
    }


    public void markBlocked(int uid) {
        preferences().putBool("blocked_" + uid, true);
        context().getUsersModule().getUsers().get(uid).getIsBlocked().change(true);
    }

    public void markNonBlocked(int uid) {
        preferences().putBool("blocked_" + uid, false);
        context().getUsersModule().getUsers().get(uid).getIsBlocked().change(false);
    }

    public boolean isUserBlocked(int uid) {
        return preferences().getBool("blocked_" + uid, false);
    }
}