package im.actor.model.mvvm;

import im.actor.model.MainThread;

/**
 * Created by ex3ndr on 19.02.15.
 */
public class MVVMEngine {
    private static MainThread mainThread;

    public static void init(MainThread mainThread) {
        MVVMEngine.mainThread = mainThread;
    }

    public static MainThread getMainThread() {
        return mainThread;
    }

    public static void runOnUiThread(Runnable runnable) {
        mainThread.runOnUiThread(runnable);
    }
}
