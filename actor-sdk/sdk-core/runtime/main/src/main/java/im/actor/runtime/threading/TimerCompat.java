/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.threading;

import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;

import static im.actor.runtime.actors.ActorSystem.system;

public class TimerCompat extends AbsTimerCompat {

    private static final ActorRef TIMER_ACTOR = system().actorOf(Props.create(TimerActor.class, new ActorCreator<TimerActor>() {
        @Override
        public TimerActor create() {
            return new TimerActor();
        }
    }), "actor/global_timer");

    public TimerCompat(Runnable runnable) {
        super(runnable);
    }

    @Override
    public synchronized void cancel() {
        TIMER_ACTOR.send(new TimerActor.Cancel(this));
    }

    @Override
    public synchronized void schedule(long delay) {
        TIMER_ACTOR.send(new TimerActor.Schedule(this, delay));
    }
}