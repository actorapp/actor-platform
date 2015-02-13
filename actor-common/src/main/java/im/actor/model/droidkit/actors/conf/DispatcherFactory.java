package im.actor.model.droidkit.actors.conf;

import im.actor.model.droidkit.actors.ActorSystem;
import im.actor.model.droidkit.actors.ThreadPriority;
import im.actor.model.droidkit.actors.mailbox.ActorDispatcher;

/**
 * Created by ex3ndr on 06.02.15.
 */
public interface DispatcherFactory {
    public ActorDispatcher createDispatcher(String name, int threadsCount, ThreadPriority priority, ActorSystem actorSystem);
}
