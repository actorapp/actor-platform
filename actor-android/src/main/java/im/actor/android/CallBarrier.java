package im.actor.android;

import android.os.Looper;

import java.util.ArrayList;

/**
 * Created by ex3ndr on 08.10.14.
 */
public class CallBarrier {
    private ArrayList<Runnable> pendingRunnable = new ArrayList<Runnable>();
    private boolean isPaused = true;

    public void call(Runnable runnable) {
        checkUiThread();
        if (isPaused) {
            pendingRunnable.add(runnable);
        } else {
            runnable.run();
        }
    }

    public void callWeak(Runnable runnable) {
        checkUiThread();
        if (!isPaused) {
            runnable.run();
        }
    }

    public void pause() {
        checkUiThread();
        isPaused = true;
    }

    public void resume() {
        checkUiThread();
        isPaused = false;
        for (Runnable runnable : pendingRunnable) {
            runnable.run();
        }
        pendingRunnable.clear();
    }

    private void checkUiThread() {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new RuntimeException("Unable to call method not from ui thread");
        }
    }
}
