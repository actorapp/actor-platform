package im.actor.runtime.promise;

import im.actor.runtime.actors.ActorRef;

public class PromiseActorDispatcher extends PromiseDispatcher {

    private ActorRef ref;

    public PromiseActorDispatcher(ActorRef ref) {
        this.ref = ref;
    }

    @Override
    public void dispatch(Promise promise, final Runnable runnable) {
        ref.send(new PromiseDispatch(promise) {
            @Override
            public void run() {
                runnable.run();
            }
        });
    }
}
