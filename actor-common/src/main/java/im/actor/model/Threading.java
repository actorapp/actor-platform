package im.actor.model;

import im.actor.model.droidkit.actors.ActorSystem;
import im.actor.model.droidkit.actors.ThreadPriority;
import im.actor.model.droidkit.actors.mailbox.ActorDispatcher;
import im.actor.model.droidkit.actors.utils.AtomicIntegerCompat;
import im.actor.model.droidkit.actors.utils.AtomicLongCompat;
import im.actor.model.droidkit.actors.utils.ThreadLocalCompat;

/**
 * Created by ex3ndr on 16.02.15.
 */
public interface Threading {

    public long getActorTime();

    public long getCurrentTime();

    public int getCoresCount();

    public AtomicIntegerCompat createAtomicInt(int init);

    public AtomicLongCompat createAtomicLong(long init);

    public <T> ThreadLocalCompat<T> createThreadLocal();

    public ActorDispatcher createDispatcher(String name, int threadsCount, ThreadPriority priority, ActorSystem actorSystem);

    public ActorDispatcher createDefaultDispatcher(String name, ThreadPriority priority, ActorSystem system);
}
