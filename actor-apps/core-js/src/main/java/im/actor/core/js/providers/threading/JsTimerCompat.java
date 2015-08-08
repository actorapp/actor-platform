/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.providers.threading;

import im.actor.core.concurrency.AbsTimerCompat;

public class JsTimerCompat extends AbsTimerCompat {

    private JsInterval secureInterval;

    public JsTimerCompat(Runnable runnable) {
        super(runnable);

        secureInterval = JsInterval.create(runnable);
    }

    @Override
    public void cancel() {
        secureInterval.cancel();
    }

    @Override
    public void schedule(long delay) {
        secureInterval.schedule((int) delay);
    }
}
