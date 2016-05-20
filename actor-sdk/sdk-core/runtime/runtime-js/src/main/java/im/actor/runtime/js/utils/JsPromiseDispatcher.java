package im.actor.runtime.js.utils;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.threading.SimpleDispatcher;

public class JsPromiseDispatcher implements SimpleDispatcher {

    public static final JsPromiseDispatcher INSTANCE = new JsPromiseDispatcher();

    public JsPromiseDispatcher() {
    }

    @Override
    public void dispatch(@NotNull Runnable runnable) {
        runnable.run();
    }
}
