package im.actor.core.modules.blocklist;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiOutPeer;
import im.actor.core.api.ApiUser;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.rpc.RequestBlockUser;
import im.actor.core.api.rpc.RequestGetReferencedEntitites;
import im.actor.core.api.rpc.RequestLoadBlockedUsers;
import im.actor.core.api.rpc.RequestUnblockUser;
import im.actor.core.api.rpc.ResponseGetReferencedEntitites;
import im.actor.core.api.rpc.ResponseLoadBlockedUsers;
import im.actor.core.api.rpc.ResponseSeq;
import im.actor.core.api.updates.UpdateUserBlocked;
import im.actor.core.api.updates.UpdateUserUnblocked;
import im.actor.core.entity.Peer;
import im.actor.core.entity.User;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;

import static im.actor.runtime.actors.ActorSystem.system;

public class BlockListModule extends AbsModule {

    private ActorRef blockListProcessor;

    public BlockListModule(final ModuleContext context) {
        super(context);

        blockListProcessor = system().actorOf("actor/blocked", () -> {
            return new BlockListActor(context);
        });
    }

    public Promise<List<User>> loadBlockedUsers() {
        return new Promise<>((PromiseFunc<List<User>>) resolver -> request(new RequestLoadBlockedUsers(), new RpcCallback<ResponseLoadBlockedUsers>() {
            @Override
            public void onResult(ResponseLoadBlockedUsers response) {
                List<ApiUserOutPeer> missingPeers = new ArrayList<>();
                final List<User> res = new ArrayList<>();
                for (ApiUserOutPeer outPeer : response.getUserPeers()) {
                    User u = users().getValue(outPeer.getUid());
                    if (u != null) {
                        res.add(u);
                    } else {
                        missingPeers.add(outPeer);
                    }
                }

                if (missingPeers.size() > 0) {
                    request(new RequestGetReferencedEntitites(missingPeers, new ArrayList<>()), new RpcCallback<ResponseGetReferencedEntitites>() {
                        @Override
                        public void onResult(final ResponseGetReferencedEntitites response) {
                            updates().executeRelatedResponse(response.getUsers(), response.getGroups(), () -> {
                                for (ApiUser usr : response.getUsers()) {
                                    res.add(users().getValue(usr.getId()));
                                }

                                resolver.result(res);
                            });
                        }

                        @Override
                        public void onError(RpcException e) {
                            resolver.error(e);
                        }
                    });
                } else {
                    resolver.result(res);
                }
            }

            @Override
            public void onError(RpcException e) {
                resolver.error(e);
            }
        }));
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

    public ActorRef getBlockListProcessor() {
        return blockListProcessor;
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