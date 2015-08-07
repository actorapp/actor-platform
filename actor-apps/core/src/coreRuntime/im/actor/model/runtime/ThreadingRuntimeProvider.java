package im.actor.model.runtime;

import im.actor.model.concurrency.AbsTimerCompat;
import im.actor.model.droidkit.actors.ActorSystem;
import im.actor.model.droidkit.actors.ThreadPriority;
import im.actor.model.droidkit.actors.mailbox.ActorDispatcher;
import im.actor.model.util.AtomicIntegerCompat;
import im.actor.model.util.AtomicLongCompat;
import im.actor.model.util.ThreadLocalCompat;

/**
 * Created by ex3ndr on 07.08.15.
 */
public class ThreadingRuntimeProvider implements ThreadingRuntime {

    @Override
    public long getActorTime() {
        throw new RuntimeException("Dump");
    }

    @Override
    public long getCurrentTime() {
        throw new RuntimeException("Dump");
    }

    @Override
    public long getSyncedCurrentTime() {
        throw new RuntimeException("Dump");
    }

    @Override
    public int getCoresCount() {
        throw new RuntimeException("Dump");
    }

    @Override
    public AtomicIntegerCompat createAtomicInt(int value) {
        throw new RuntimeException("Dump");
    }

    @Override
    public AtomicLongCompat createAtomicLong(long value) {
        throw new RuntimeException("Dump");
    }

    @Override
    public <T> ThreadLocalCompat<T> createThreadLocal() {
        throw new RuntimeException("Dump");
    }

    @Override
    public AbsTimerCompat createTimer(Runnable runnable) {
        throw new RuntimeException("Dump");
    }

    @Override
    public ActorDispatcher createDispatcher(String name, int threadsCount, ThreadPriority priority, ActorSystem actorSystem) {
        throw new RuntimeException("Dump");
    }

    @Override
    public ActorDispatcher createDefaultDispatcher(String name, ThreadPriority priority, ActorSystem system) {
        throw new RuntimeException("Dump");
    }
}
