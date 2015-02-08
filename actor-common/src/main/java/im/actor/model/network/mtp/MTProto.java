package im.actor.model.network.mtp;

import com.droidkit.actors.ActorRef;
import im.actor.model.network.Endpoints;
import im.actor.model.network.mtp.actors.ManagerActor;
import im.actor.model.network.mtp.actors.ReceiverActor;
import im.actor.model.network.mtp.actors.SenderActor;
import im.actor.model.network.mtp.entity.MTRpcRequest;
import im.actor.model.network.mtp.entity.ProtoStruct;
import im.actor.model.network.util.MTUids;

/**
 * Created by ex3ndr on 07.02.15.
 */
public class MTProto {
    private final long authId;
    private final long sessionId;
    private final Endpoints endpoints;
    private final MTProtoCallback callback;

    private final ActorRef receiver;
    private final ActorRef manager;
    private final ActorRef sender;

    private final String actorPath = "mtproto";

    public MTProto(long authId, long sessionId, Endpoints endpoints, MTProtoCallback callback) {
        this.authId = authId;
        this.sessionId = sessionId;
        this.endpoints = endpoints;
        this.callback = callback;
        this.manager = ManagerActor.manager(this);
        this.sender = SenderActor.senderActor(this);
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

    public long sendRpcMessage(ProtoStruct protoStruct) {
        long mtId = MTUids.nextId();
        sender.send(new SenderActor.SendMessage(mtId, new MTRpcRequest(protoStruct.toByteArray()).toByteArray()));
        return mtId;
    }

    public void cancelRpc(long mtId) {
        sender.send(new SenderActor.ForgetMessage(mtId));
    }
}
