package im.actor.runtime.js.threading;

import im.actor.runtime.threading.Dispatcher;

public class JsDispatcher implements Dispatcher {

    public JsDispatcher() {
        
    }

    @Override
    public void dispatch(Runnable message, long delay) {
        dispatchJs(message, (int) delay);
    }

    public native final void dispatchJs(Runnable runnable, int msec)/*-{
        var _runnable = runnable
        setTimeout(function() {
            _runnable.@java.lang.Runnable::run()();
        }, msec);
    }-*/;
}