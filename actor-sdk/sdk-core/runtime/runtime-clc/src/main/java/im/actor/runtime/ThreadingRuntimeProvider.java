package im.actor.runtime;

import im.actor.runtime.clc.ClcDispatcher;
import im.actor.runtime.generic.*;
import im.actor.runtime.threading.Dispatcher;

public class ThreadingRuntimeProvider extends GenericThreadingProvider {

    public ThreadingRuntimeProvider(){
    }

    @Override
    public Dispatcher createDispatcher(String name) {
        return new ClcDispatcher(name);
    }
}
