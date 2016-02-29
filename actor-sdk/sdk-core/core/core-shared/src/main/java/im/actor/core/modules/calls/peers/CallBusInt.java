package im.actor.core.modules.calls.peers;

import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;

public class CallBusInt extends ActorInterface {

    public CallBusInt(ActorRef dest) {
        super(dest);
    }

    public void joinBus(String busId) {
        send(new CallBusActor.JoinBus(busId));
    }

    public void changeMute(boolean isMuted) {
        send(new CallBusActor.Mute(isMuted));
    }

    public void startOwn() {
        send(new CallBusActor.OnAnswered());
    }
}