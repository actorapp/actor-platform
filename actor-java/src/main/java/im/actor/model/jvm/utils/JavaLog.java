package im.actor.model.jvm.utils;

import im.actor.model.log.LogInterface;

/**
 * Created by ex3ndr on 07.02.15.
 */
public class JavaLog implements LogInterface {
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
