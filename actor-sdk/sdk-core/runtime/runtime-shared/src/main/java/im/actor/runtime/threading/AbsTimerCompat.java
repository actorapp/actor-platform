/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.threading;

public abstract class AbsTimerCompat {

    private Runnable runnable;

    public AbsTimerCompat(Runnable runnable) {
        this.runnable = runnable;
    }

    protected void invokeRun() {
        runnable.run();
    }

    public abstract void cancel();

    public abstract void schedule(long delay);
}