package im.actor.runtime.actors;

import im.actor.runtime.Log;
import im.actor.runtime.threading.DispatchCancel;
import im.actor.runtime.threading.Dispatcher;

public class Scheduler {

    private static final boolean LOG = false;
    private static final String TAG = "Scheduler";

    private static final Dispatcher TIMER_DISPATCHER = im.actor.runtime.Runtime.createDispatcher("scheduler");

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
        if (LOG) {
            Log.d(TAG, "schedule " + ref.getPath());
        }
        final TaskCancellable res = new TaskCancellable();
        res.setDispatchCancel(destDispatcher.dispatch(new Runnable() {
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
        }, delay));
        return res;
    }

    private class TaskCancellable implements Cancellable {

        private volatile boolean isCancelled = false;
        private volatile DispatchCancel dispatchCancel;

        public boolean isCancelled() {
            return isCancelled;
        }

        public synchronized void setDispatchCancel(DispatchCancel dispatchCancel) {
            if (isCancelled) {
                dispatchCancel.cancel();
            } else {
                this.dispatchCancel = dispatchCancel;
            }
        }

        @Override
        public synchronized void cancel() {
            if (!isCancelled) {
                if (LOG) {
                    Log.d(TAG, "Cancel " + ref.getPath());
                }
                isCancelled = true;
                if (this.dispatchCancel != null) {
                    this.dispatchCancel.cancel();
                }
            }
        }
    }
}
