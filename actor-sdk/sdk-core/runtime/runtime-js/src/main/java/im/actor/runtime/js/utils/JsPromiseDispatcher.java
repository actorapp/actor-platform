package im.actor.runtime.js.utils;

import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseDispatcher;

public class JsPromiseDispatcher extends PromiseDispatcher {

    public static final JsPromiseDispatcher INSTANCE = new JsPromiseDispatcher();

    public JsPromiseDispatcher() {
    }

    @Override
    public void dispatch(Promise promise, Runnable runnable) {
        runnable.run();
    }
}
