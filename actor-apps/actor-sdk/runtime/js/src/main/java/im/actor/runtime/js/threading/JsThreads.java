/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.threading;

import im.actor.runtime.actors.ActorTime;
import im.actor.runtime.actors.dispatch.AbstractDispatchQueue;
import im.actor.runtime.actors.dispatch.AbstractDispatcher;
import im.actor.runtime.actors.dispatch.Dispatch;
import im.actor.runtime.actors.dispatch.DispatchResult;

public class JsThreads<T, Q extends AbstractDispatchQueue<T>> extends AbstractDispatcher<T, Q> {

    private static final int ITERATION_COUNT_MAX = 10;

    private JsSecureInterval secureInterval;
    private boolean isDoingIteration = false;

    protected JsThreads(Q queue, Dispatch<T> dispatch) {
        super(queue, dispatch);
        secureInterval = JsSecureInterval.create(new Runnable() {
            @Override
            public void run() {
                isDoingIteration = true;
                long delay = -1;
                int iteration = 0;
                while (delay < 0 && iteration < ITERATION_COUNT_MAX) {
                    delay = doIteration();
                    iteration++;
                }
                isDoingIteration = false;
                if (delay < 0) {
                    secureInterval.scheduleNow();
                } else {
                    if (delay > 15000) {
                        delay = 15000;
                    }
                    if (delay < 1) {
                        delay = 1;
                    }

                    secureInterval.schedule((int) delay);
                }
            }
        });
    }

    @Override
    protected void notifyDispatcher() {
        if (!isDoingIteration) {
            secureInterval.scheduleNow();
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
