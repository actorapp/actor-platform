package im.actor.runtime;

import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.ThreadPriority;
import im.actor.runtime.actors.mailbox.ActorDispatcher;
import im.actor.runtime.threading.AbsTimerCompat;
import im.actor.runtime.threading.AtomicIntegerCompat;
import im.actor.runtime.threading.AtomicLongCompat;
import im.actor.runtime.threading.ThreadLocalCompat;

/**
 * Created by ex3ndr on 07.08.15.
 */
public class ThreadingRuntimeProvider implements ThreadingRuntime {

    @Override
    public long getActorTime() {
        throw new RuntimeException("Dumb");
    }

    @Override
    public long getCurrentTime() {
        throw new RuntimeException("Dumb");
    }

    @Override
    public long getSyncedCurrentTime() {
        throw new RuntimeException("Dumb");
    }

    @Override
    public int getCoresCount() {
        throw new RuntimeException("Dumb");
    }

    @Override
    public AtomicIntegerCompat createAtomicInt(int value) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public AtomicLongCompat createAtomicLong(long value) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public <T> ThreadLocalCompat<T> createThreadLocal() {
        throw new RuntimeException("Dumb");
    }

    @Override
    public AbsTimerCompat createTimer(Runnable runnable) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public ActorDispatcher createDispatcher(String name, int threadsCount, ThreadPriority priority, ActorSystem actorSystem) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public ActorDispatcher createDefaultDispatcher(String name, ThreadPriority priority, ActorSystem system) {
        throw new RuntimeException("Dumb");
    }
}
