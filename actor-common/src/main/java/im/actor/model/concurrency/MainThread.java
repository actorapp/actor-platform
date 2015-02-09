package im.actor.model.concurrency;

/**
 * Created by ex3ndr on 08.02.15.
 */
public interface MainThread {
    public void runOnUiThread(Runnable runnable);
}
