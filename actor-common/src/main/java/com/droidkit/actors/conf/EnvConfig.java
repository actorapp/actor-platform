package com.droidkit.actors.conf;

import com.droidkit.actors.ActorSystem;
import com.droidkit.actors.ThreadPriority;
import com.droidkit.actors.mailbox.ActorDispatcher;

/**
 * Created by ex3ndr on 06.02.15.
 */
public class EnvConfig {

    private EnvConfig() {
    }

    private static DispatcherFactory dispatcherFactory;

    public static DispatcherFactory getDispatcherFactory() {
        return dispatcherFactory;
    }

    public static void setDispatcherFactory(DispatcherFactory dispatcherFactory) {
        EnvConfig.dispatcherFactory = dispatcherFactory;
    }

    public static ActorDispatcher createDispatcher(String name, int threadsCount, ThreadPriority priority, ActorSystem actorSystem) {
        if (dispatcherFactory == null) {
            throw new RuntimeException("EnvConfig not inited!");
        }
        return dispatcherFactory.createDispatcher(name, threadsCount, priority, actorSystem);
    }
}
