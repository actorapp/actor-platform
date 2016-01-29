package im.actor.runtime.actors;

import im.actor.runtime.actors.ask.AskIntRequest;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.ask.AskResult;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.PromiseResolver;

public class ActorInterface {

    private ActorRef dest;

    public ActorInterface(ActorRef dest) {
        this.dest = dest;
    }

    protected void send(Object message) {
        dest.send(message);
    }

    protected <T> Promise<T> ask(final AskMessage<T> message) {
        return new Promise<T>(new PromiseFunc<T>() {
            @Override
            public void exec(PromiseResolver<T> executor) {
                send(new AskIntRequest(message, executor));
            }
        });
    }
}
