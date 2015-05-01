package im.actor.model.concurrency;

import im.actor.model.droidkit.actors.Actor;

/**
 * Created by ex3ndr on 01.05.15.
 */
public class TimerActor extends Actor {

    @Override
    public void onReceive(Object message) {
        if (message instanceof Schedule) {
            self().sendOnce(((Schedule) message).getTimerCompat(), ((Schedule) message).getDelay());
        } else if (message instanceof Cancel) {
            self().cancelMessage(((Cancel) message).getTimerCompat());
        } else if (message instanceof TimerCompat) {
            ((TimerCompat) message).invokeRun();
        } else {
            drop(message);
        }
    }

    public static class Cancel {
        private TimerCompat timerCompat;

        public Cancel(TimerCompat timerCompat) {
            this.timerCompat = timerCompat;
        }

        public TimerCompat getTimerCompat() {
            return timerCompat;
        }
    }

    public static class Schedule {
        private TimerCompat timerCompat;
        private long delay;

        public Schedule(TimerCompat timerCompat, long delay) {
            this.timerCompat = timerCompat;
            this.delay = delay;
        }

        public long getDelay() {
            return delay;
        }

        public TimerCompat getTimerCompat() {
            return timerCompat;
        }
    }
}
