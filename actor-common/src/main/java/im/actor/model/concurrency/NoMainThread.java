package im.actor.model.concurrency;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class NoMainThread implements MainThread {
    @Override
    public void runOnUiThread(Runnable runnable) {
        runnable.run();
    }
}
