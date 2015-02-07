package im.actor.model.network.mtp;

import com.droidkit.actors.ActorRef;
import im.actor.model.network.Endpoints;
import im.actor.model.network.mtp.actors.ManagerActor;
import im.actor.model.network.mtp.actors.ReceiverActor;
import im.actor.model.network.mtp.actors.SenderActor;
import im.actor.model.network.mtp.entity.ProtoStruct;
import im.actor.model.network.util.MTUids;

/**
 * Created by ex3ndr on 07.02.15.
 */
public class MTProto {
    private final long authId;
    private final long sessionId;
    private final Endpoints endpoints;

    private final ActorRef receiver;
    private final ActorRef manager;
    private final ActorRef sender;

    private final String actorPath = "mtproto";

    public MTProto(long authId, long sessionId, Endpoints endpoints) {
        this.authId = authId;
        this.sessionId = sessionId;
        this.endpoints = endpoints;
        this.manager = ManagerActor.manager(this);
        this.sender = SenderActor.senderActor(this);
        this.receiver = ReceiverActor.receiver(this);
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

    public void sendMTMessage(ProtoStruct protoStruct) {
        sender.send(new SenderActor.SendMessage(MTUids.nextId(), protoStruct.toByteArray()));
    }
}
