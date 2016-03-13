package im.actor.runtime.cocoa.threading;

import im.actor.runtime.threading.Dispatcher;

public class CocoaDispatcher implements Dispatcher {

    public static CocoaDispatcherProxy dispatcherProxy;

    public static CocoaDispatcherProxy getDispatcherProxy() {
        return dispatcherProxy;
    }

    public static void setDispatcherProxy(CocoaDispatcherProxy dispatcherProxy) {
        CocoaDispatcher.dispatcherProxy = dispatcherProxy;
    }

    @Override
    public void dispatch(Runnable message, long delay) {
        dispatcherProxy.dispatchOnBackground(message, delay);
    }
}
