package im.actor.runtime.actors;

import im.actor.runtime.actors.ask.AskIntRequest;
import im.actor.runtime.actors.promise.PromiseResolver;

public class AskcableActor extends Actor {

    public void onAsk(Object message, PromiseResolver future) {
        future.error(new RuntimeException("Not implemented"));
        drop(message);
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof AskIntRequest) {
            AskIntRequest askRequest = (AskIntRequest) message;
            onAsk(askRequest.getMessage(), askRequest.getFuture());
        } else {
            super.onReceive(message);
        }
    }
}
