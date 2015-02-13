package com.droidkit.engine._internal.util;

import android.os.Looper;

public class Utils {

    public static boolean isUIThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }
}
