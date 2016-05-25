package im.actor.core.modules.calls.peers;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;

public class CallBusInt extends ActorInterface {

    public CallBusInt(@NotNull ActorRef dest) {
        super(dest);
    }

    public void joinBus(@NotNull String busId) {
        send(new CallBusActor.JoinBus(busId));
    }

    public void joinMasterBus(@NotNull String busId, long deviceId) {
        send(new CallBusActor.JoinMasterBus(busId, deviceId));
    }

    public void changeMute(boolean isMuted) {
        send(new CallBusActor.Mute(isMuted));
    }

    public void changeVideoEnabled(boolean enabled) {
        send(new CallBusActor.VideoEnabled(enabled));
    }

    public void startOwn() {
        send(new CallBusActor.OnAnswered());
    }
}