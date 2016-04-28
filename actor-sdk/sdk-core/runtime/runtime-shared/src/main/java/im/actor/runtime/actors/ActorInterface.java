package im.actor.runtime.actors;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.Log;
import im.actor.runtime.actors.ask.AskIntRequest;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.PromiseResolver;

public abstract class ActorInterface {

    @NotNull
    private ActorRef dest;

    public ActorInterface(@NotNull ActorRef dest) {
        this.dest = dest;
    }

    protected ActorInterface() {

    }

    protected void setDest(@NotNull ActorRef ref) {
        this.dest = ref;
    }

    @NotNull
    public ActorRef getDest() {
        return dest;
    }

    protected void send(Object message) {
        dest.send(message);
    }

    protected <T> Promise<T> ask(@NotNull final AskMessage<T> message) {
        return new Promise<>(new PromiseFunc<T>() {
            @Override
            public void exec(@NotNull PromiseResolver<T> executor) {
                send(new AskIntRequest(message, executor));
            }
        });
    }

    public void kill() {
        dest.send(PoisonPill.INSTANCE);
    }
}
