package im.actor.runtime.actors;

import im.actor.runtime.actors.ask.AskRequest;
import im.actor.runtime.actors.future.Future;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.actors.promise.Promise;
import im.actor.runtime.actors.promise.PromiseExecutor;

public class AskcableActor extends Actor {

    public void onAsk(Object message, PromiseExecutor future) {
        future.error(new RuntimeException("Not implemented"));
        drop(message);
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof AskRequest) {
            AskRequest askRequest = (AskRequest) message;
            onAsk(askRequest.getMessage(), askRequest.getFuture());
        } else {
            super.onReceive(message);
        }
    }
}
