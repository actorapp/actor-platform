/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.mtp;

import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.core.network.Endpoints;
import im.actor.core.network.NetworkState;
import im.actor.core.network.mtp.actors.ManagerActor;
import im.actor.core.network.mtp.actors.ReceiverActor;
import im.actor.core.network.mtp.actors.PusherActor;
import im.actor.core.network.mtp.entity.MTRpcRequest;
import im.actor.core.network.mtp.entity.ProtoStruct;
import im.actor.core.network.util.MTUids;

public class MTProto {
    private final long authId;
    private final long sessionId;
    private final Endpoints endpoints;
    private final MTProtoCallback callback;

    private final ActorRef receiver;
    private final ActorRef manager;
    private final ActorRef sender;

    private final String actorPath;

    private final boolean isEnableLog;
    private final int minDelay;
    private final int maxDelay;
    private final int maxFailureCount;

    private boolean isClosed;

    public MTProto(long authId,
                   long sessionId,
                   Endpoints endpoints,
                   MTProtoCallback callback,
                   boolean isEnableLog,
                   String basePath,
                   int minDelay,
                   int maxDelay,
                   int maxFailureCount) {
        this.authId = authId;
        this.sessionId = sessionId;
        this.endpoints = endpoints;
        this.callback = callback;
        this.actorPath = basePath;
        this.isEnableLog = isEnableLog;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.maxFailureCount = maxFailureCount;
        this.isClosed = false;
        this.manager = ManagerActor.manager(this);
        this.sender = PusherActor.senderActor(this);
        this.receiver = ReceiverActor.receiver(this);
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

    public boolean isEnableLog() {
        return isEnableLog;
    }

    public long sendRpcMessage(ProtoStruct protoStruct) {
        long mtId = MTUids.nextId();
        sender.send(new PusherActor.SendMessage(mtId, new MTRpcRequest(protoStruct.toByteArray()).toByteArray()));
        return mtId;
    }

    public void cancelRpc(long mtId) {
        sender.send(new PusherActor.ForgetMessage(mtId));
    }

    public void onNetworkChanged(NetworkState state) {
        this.manager.send(new ManagerActor.NetworkChanged(state));
    }

    public void forceNetworkCheck() {
        this.manager.send(new ManagerActor.ForceNetworkCheck());
    }

    public void stopProto() {
        this.sender.send(PoisonPill.INSTANCE);
        this.manager.send(PoisonPill.INSTANCE);
        this.receiver.send(PoisonPill.INSTANCE);
        this.isClosed = true;
    }

    public int getMinDelay() {
        return minDelay;
    }

    public int getMaxDelay() {
        return maxDelay;
    }

    public int getMaxFailureCount() {
        return maxFailureCount;
    }
}
