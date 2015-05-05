/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers.threading;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;

import im.actor.model.droidkit.actors.ActorTime;
import im.actor.model.droidkit.actors.dispatch.AbstractDispatchQueue;
import im.actor.model.droidkit.actors.dispatch.AbstractDispatcher;
import im.actor.model.droidkit.actors.dispatch.Dispatch;
import im.actor.model.droidkit.actors.dispatch.DispatchResult;

public class JsThreads<T, Q extends AbstractDispatchQueue<T>> extends AbstractDispatcher<T, Q> {

    private boolean isSchedulled = false;

    private Timer timer = new Timer() {
        @Override
        public void run() {
            doIteration();
        }
    };
    private Scheduler.ScheduledCommand scheduledCommand = new Scheduler.ScheduledCommand() {
        @Override
        public void execute() {
            doIteration();
        }
    };
    private Scheduler.RepeatingCommand repeatingCommand = new Scheduler.RepeatingCommand() {
        @Override
        public boolean execute() {
            long delay = doIteration();
            if (delay < 0) {
                isSchedulled = true;
                return true;
            } else {

                if (delay > 15000) {
                    delay = 15000;
                }
                if (delay < 1) {
                    delay = 1;
                }

                timer.schedule((int) delay);

                isSchedulled = false;
                return false;
            }
        }
    };

    protected JsThreads(Q queue, Dispatch<T> dispatch) {
        super(queue, dispatch);
    }

    @Override
    protected void notifyDispatcher() {
        if (!isSchedulled) {
            timer.cancel();
            Scheduler.get().scheduleIncremental(repeatingCommand);
            isSchedulled = true;
        }
    }

    protected long doIteration() {
        long time = ActorTime.currentTime();
        DispatchResult action = getQueue().dispatch(time);
        if (action.isResult()) {
            dispatchMessage((T) action.getRes());
            return -1;
        } else {
            if (action.getDelay() < 1) {
                return 1;
            } else {
                return action.getDelay();
            }
        }
    }
}
