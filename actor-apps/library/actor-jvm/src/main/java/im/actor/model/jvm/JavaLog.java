/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.jvm;

import im.actor.model.LogProvider;

public class JavaLog implements LogProvider {
    @Override
    public void w(String tag, String message) {
        System.out.println("[W]" + tag + ":" + message);
    }

    @Override
    public void e(String tag, Throwable throwable) {
        System.err.print("[E]" + tag + ":");
        throwable.printStackTrace();
    }

    @Override
    public void d(String tag, String message) {
        System.out.println("[D]" + tag + ":" + message);
    }

    @Override
    public void v(String tag, String message) {
        System.out.println("[V]" + tag + ":" + message);
    }
}
