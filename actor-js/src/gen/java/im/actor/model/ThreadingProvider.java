package im.actor.model;

import im.actor.model.droidkit.actors.ActorSystem;
import im.actor.model.droidkit.actors.ThreadPriority;
import im.actor.model.droidkit.actors.mailbox.ActorDispatcher;
import im.actor.model.util.AtomicIntegerCompat;
import im.actor.model.util.AtomicLongCompat;
import im.actor.model.util.ThreadLocalCompat;

/**
 * Created by ex3ndr on 16.02.15.
 */
public interface ThreadingProvider {

    public long getActorTime();

    public long getCurrentTime();

    public int getCoresCount();

    public AtomicIntegerCompat createAtomicInt(int value);

    public AtomicLongCompat createAtomicLong(long value);

    public <T> ThreadLocalCompat<T> createThreadLocal();

    public ActorDispatcher createDispatcher(String name, int threadsCount, ThreadPriority priority, ActorSystem actorSystem);

    public ActorDispatcher createDefaultDispatcher(String name, ThreadPriority priority, ActorSystem system);
}
