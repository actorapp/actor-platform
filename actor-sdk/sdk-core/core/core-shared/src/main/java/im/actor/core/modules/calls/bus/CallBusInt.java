package im.actor.core.modules.calls.bus;

import im.actor.core.api.ApiWebRTCSignaling;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;

public class CallBusInt extends ActorInterface {

    public CallBusInt(ActorRef dest) {
        super(dest);
    }

    public void joinBus(String busId) {
        send(new CallBusActor.JoinBus(busId));
    }

    public void createBus() {
        send(new CallBusActor.CreateBus());
    }

    public void sendSignal(int uid, long deviceId, ApiWebRTCSignaling signal) {
        send(new CallBusActor.SendSignal(uid, deviceId, signal));
    }
}
