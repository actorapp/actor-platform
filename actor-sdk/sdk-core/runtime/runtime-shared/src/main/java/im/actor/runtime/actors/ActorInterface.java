package im.actor.runtime.actors;

import im.actor.runtime.actors.ask.AskIntRequest;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.ask.AskResult;
import im.actor.runtime.promise.Promise;

public class ActorInterface {

    private ActorRef dest;

    public ActorInterface(ActorRef dest) {
        this.dest = dest;
    }

    protected void send(Object message) {
        dest.send(message);
    }

    protected  <T extends AskResult> Promise<T> ask(AskMessage<T> message) {
        return new Promise<>(executor -> send(new AskIntRequest(message, executor)));
    }
}
