/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.updates.internal;

public class ExecuteAfter {
    private int seq;
    private Runnable runnable;

    public ExecuteAfter(int seq, Runnable runnable) {
        this.seq = seq;
        this.runnable = runnable;
    }

    public int getSeq() {
        return seq;
    }

    public Runnable getRunnable() {
        return runnable;
    }
}
