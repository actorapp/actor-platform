package im.actor.model.modules.updates.internal;

/**
 * Created by ex3ndr on 04.03.15.
 */
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
