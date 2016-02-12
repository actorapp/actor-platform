package im.actor.runtime.actors;

import im.actor.runtime.threading.Dispatcher;

public class Scheduler {

    private static final Dispatcher TIMER_DISPATCHER = im.actor.runtime.Runtime.createDispatcher();

    private Dispatcher destDispatcher;
    private ActorRef ref;

    public Scheduler(ActorRef ref) {
        this(ref, TIMER_DISPATCHER);
    }

    public Scheduler(ActorRef ref, Dispatcher destDispatcher) {
        this.ref = ref;
        this.destDispatcher = destDispatcher;
    }

    public Cancellable schedule(final Runnable runnable, long delay) {
        final TaskCancellable res = new TaskCancellable();
        destDispatcher.dispatch(new Runnable() {
            @Override
            public void run() {
                if (res.isCancelled()) {
                    return;
                }
                ref.send(new Runnable() {
                    @Override
                    public void run() {
                        if (res.isCancelled()) {
                            return;
                        }
                        runnable.run();
                    }
                });
            }
        }, delay);
        return res;
    }

    private class TaskCancellable implements Cancellable {

        private volatile boolean isCancelled = false;

        public boolean isCancelled() {
            return isCancelled;
        }

        @Override
        public void cancel() {
            isCancelled = true;
        }
    }
}
