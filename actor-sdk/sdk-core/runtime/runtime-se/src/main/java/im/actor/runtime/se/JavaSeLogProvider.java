/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.se;

import im.actor.runtime.LogRuntime;

public class JavaSeLogProvider implements LogRuntime {
    @Override
    public synchronized void w(String tag, String message) {
        System.out.println(tag + "[w]: " + message);
    }

    @Override
    public void e(String tag, Throwable throwable) {
        System.out.println(tag + "[e]: " + throwable);
    }

    @Override
    public void d(String tag, String message) {
        System.out.println(tag + "[d]: " + message);
    }

    @Override
    public void v(String tag, String message) {
        System.out.println(tag + "[v]: " + message);
    }
}
