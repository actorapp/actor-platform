/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

import im.actor.model.MainThreadProvider;

/**
 * Created by ex3ndr on 19.02.15.
 */
public class MVVMEngine {
    private static MainThreadProvider mainThreadProvider;

    public static void init(MainThreadProvider mainThreadProvider) {
        MVVMEngine.mainThreadProvider = mainThreadProvider;
    }

    public static MainThreadProvider getMainThreadProvider() {
        return mainThreadProvider;
    }

    public static void checkMainThread() {
        if (mainThreadProvider.isSingleThread()) {
            return;
        }
        if (!mainThreadProvider.isMainThread()) {
            throw new RuntimeException("Unable to perform operation not from Main Thread");
        }
    }

    // TODO: Rename to runOnMainThread
    public static void runOnUiThread(Runnable runnable) {
        mainThreadProvider.postToMainThread(runnable);
    }
}
