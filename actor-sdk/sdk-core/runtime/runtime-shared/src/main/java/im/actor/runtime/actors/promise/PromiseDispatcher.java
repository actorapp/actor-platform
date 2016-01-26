package im.actor.runtime.actors.promise;

import im.actor.runtime.actors.ActorRef;

public abstract class PromiseDispatcher {

    public abstract void dispatch(Runnable runnable);

    public static PromiseDispatcher DEFAULT = new PromiseDispatcher() {
        @Override
        public void dispatch(Runnable runnable) {
            im.actor.runtime.Runtime.dispatch(runnable);
        }
    };

    public static PromiseDispatcher forActor(final ActorRef ref) {
        return new PromiseDispatcher() {
            @Override
            public void dispatch(Runnable runnable) {
                ref.send(runnable);
            }
        };
    }
}
