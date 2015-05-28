/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network.mtp;

import im.actor.model.NetworkProvider;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.network.Endpoints;
import im.actor.model.network.NetworkState;
import im.actor.model.network.mtp.actors.ManagerActor;
import im.actor.model.network.mtp.actors.ReceiverActor;
import im.actor.model.network.mtp.actors.SenderActor;
import im.actor.model.network.mtp.entity.MTRpcRequest;
import im.actor.model.network.mtp.entity.ProtoStruct;
import im.actor.model.network.util.MTUids;

public class MTProto {
    private final long authId;
    private final long sessionId;
    private final Endpoints endpoints;
    private final MTProtoCallback callback;
    private final NetworkProvider networkProvider;

    private final ActorRef receiver;
    private final ActorRef manager;
    private final ActorRef sender;

    private final String actorPath = "mtproto";

    public MTProto(long authId,
                   long sessionId,
                   Endpoints endpoints,
                   MTProtoCallback callback,
                   NetworkProvider networkProvider) {
        this.authId = authId;
        this.sessionId = sessionId;
        this.endpoints = endpoints;
        this.callback = callback;
        this.networkProvider = networkProvider;
        this.manager = ManagerActor.manager(this);
        this.sender = SenderActor.senderActor(this);
        this.receiver = ReceiverActor.receiver(this);
    }

    public NetworkProvider getNetworkProvider() {
        return networkProvider;
    }

    public MTProtoCallback getCallback() {
        return callback;
    }

    public Endpoints getEndpoints() {
        return endpoints;
    }

    public long getAuthId() {
        return authId;
    }

    public long getSessionId() {
        return sessionId;
    }

    public String getActorPath() {
        return actorPath;
    }

    public long sendRpcMessage(ProtoStruct protoStruct) {
        long mtId = MTUids.nextId();
        sender.send(new SenderActor.SendMessage(mtId, new MTRpcRequest(protoStruct.toByteArray()).toByteArray()));
        return mtId;
    }

    public void cancelRpc(long mtId) {
        sender.send(new SenderActor.ForgetMessage(mtId));
    }

    public void onNetworkChanged(NetworkState state) {
        this.manager.send(new ManagerActor.NetworkChanged(state));
    }
}
