package im.actor.runtime.promise;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.threading.SimpleDispatcher;

public class SimpleActorDispatcher implements SimpleDispatcher {

    private ActorRef ref;

    public SimpleActorDispatcher(ActorRef ref) {
        this.ref = ref;
    }

    @Override
    public void dispatch(@NotNull final Runnable runnable) {
        ref.send(new PromiseDispatch() {
            @Override
            public void run() {
                runnable.run();
            }
        });
    }
}
