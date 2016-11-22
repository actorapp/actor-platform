package im.actor.runtime.threading;

import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorCancellable;
import im.actor.runtime.actors.Scheduler;

import static im.actor.runtime.actors.ActorSystem.system;

public class CommonTimer {

    private static final ActorRef COMMON_TIMER_ACTOR = system().actorOf("common_timer", new ActorCreator() {
        @Override
        public Actor create() {
            return new Actor();
        }
    });
    private static final Scheduler COMMON_SCHEDULER = new Scheduler(COMMON_TIMER_ACTOR);

    private final Runnable runnable;
    private ActorCancellable lastSchedule;
    private boolean isDisposed;

    public CommonTimer(Runnable runnable) {
        this.runnable = runnable;
    }

    public void schedule(long time) {
        if (isDisposed) {
            return;
        }

        if (lastSchedule != null) {
            lastSchedule.cancel();
        }

        lastSchedule = COMMON_SCHEDULER.schedule(runnable, time);
    }

    public void cancel() {
        if (lastSchedule != null) {
            lastSchedule.cancel();
        }
    }

    public void dispose() {
        isDisposed = true;
        cancel();
    }
}
