package im.actor.runtime.generic;

import im.actor.runtime.generic.threading.GenericAtomicInteger;
import im.actor.runtime.generic.threading.GenericAtomicLong;
import im.actor.runtime.generic.threading.GenericDispatcherActor;
import im.actor.runtime.generic.threading.GenericThreadLocal;
import im.actor.runtime.ThreadingRuntime;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.ThreadPriority;
import im.actor.runtime.actors.mailbox.ActorDispatcher;
import im.actor.runtime.threading.AbsTimerCompat;
import im.actor.runtime.threading.AtomicIntegerCompat;
import im.actor.runtime.threading.AtomicLongCompat;
import im.actor.runtime.threading.ThreadLocalCompat;
import im.actor.runtime.threading.TimerCompat;

/**
 * Created by ex3ndr on 07.08.15.
 */
public class GenericThreadingProvider implements ThreadingRuntime {

    public GenericThreadingProvider() {

    }

    @Override
    public long getActorTime() {
        return System.nanoTime() / 1000000;
    }

    @Override
    public long getCurrentTime() {
        return System.currentTimeMillis();
    }

    @Override
    public long getSyncedCurrentTime() {
        return getCurrentTime();
    }

    @Override
    public int getCoresCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    @Override
    public AtomicIntegerCompat createAtomicInt(int value) {
        return new GenericAtomicInteger(value);
    }

    @Override
    public AtomicLongCompat createAtomicLong(long value) {
        return new GenericAtomicLong(value);
    }

    @Override
    public <T> ThreadLocalCompat<T> createThreadLocal() {
        return new GenericThreadLocal<T>();
    }

    @Override
    public AbsTimerCompat createTimer(Runnable runnable) {
        return new TimerCompat(runnable);
    }

    @Override
    public ActorDispatcher createDispatcher(String name, int threadsCount, ThreadPriority priority, ActorSystem actorSystem) {
        return new GenericDispatcherActor(name, actorSystem, threadsCount, priority);
    }

    @Override
    public ActorDispatcher createDefaultDispatcher(String name, ThreadPriority priority, ActorSystem actorSystem) {
        return createDispatcher(name, getCoresCount() * 2, priority, actorSystem);
    }
}
