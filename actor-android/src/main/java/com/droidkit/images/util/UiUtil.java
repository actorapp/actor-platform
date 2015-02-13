package com.droidkit.images.util;

import android.os.Looper;

/**
 * Created by ex3ndr on 27.08.14.
 */
public class UiUtil {
    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }
}
