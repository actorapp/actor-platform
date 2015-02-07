package com.droidkit.actors.conf;

import com.droidkit.actors.ActorSystem;
import com.droidkit.actors.ThreadPriority;
import com.droidkit.actors.mailbox.ActorDispatcher;

/**
 * Created by ex3ndr on 06.02.15.
 */
public interface DispatcherFactory {
    public ActorDispatcher createDispatcher(String name, int threadsCount, ThreadPriority priority, ActorSystem actorSystem);
}
