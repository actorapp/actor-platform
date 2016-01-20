package im.actor.runtime.actors;

import im.actor.runtime.actors.ask.AskRequest;
import im.actor.runtime.actors.messages.Void;

public class AskcableActor extends Actor {

    public boolean onAsk(Object message, Future future) {
        drop(message);
        return false;
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof AskRequest) {
            AskRequest askRequest = (AskRequest) message;
            if (onAsk(askRequest.getMessage(), askRequest.getFuture())) {
                askRequest.getFuture().onResult(Void.INSTANCE);
            }
        } else {
            super.onReceive(message);
        }
    }
}
