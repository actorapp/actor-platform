package im.actor.runtime.js.threading;

import im.actor.runtime.threading.DispatchCancel;
import im.actor.runtime.threading.Dispatcher;

public class JsDispatcher implements Dispatcher {

    public JsDispatcher() {

    }

    @Override
    public DispatchCancel dispatch(Runnable message, long delay) {
        final JsCanceller canceller = dispatchJs(message, (int) delay);
        return new DispatchCancel() {
            @Override
            public void cancel() {
                canceller.cancel();
            }
        };
    }

    public native final JsCanceller dispatchJs(Runnable runnable, int msec)/*-{
        var _runnable = runnable
        return setTimeout(function() {
            _runnable.@java.lang.Runnable::run()();
        }, msec);
    }-*/;
}