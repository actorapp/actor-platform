/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.runtime;

import im.actor.model.concurrency.AbsTimerCompat;
import im.actor.model.concurrency.TimerCompat;
import im.actor.model.droidkit.actors.ActorSystem;
import im.actor.model.droidkit.actors.ThreadPriority;
import im.actor.model.droidkit.actors.mailbox.ActorDispatcher;
import im.actor.model.runtime.threads.JavaAtomicInteger;
import im.actor.model.runtime.threads.JavaAtomicLong;
import im.actor.model.runtime.threads.JavaDispatcherActor;
import im.actor.model.runtime.threads.JavaThreadLocal;
import im.actor.model.util.AtomicIntegerCompat;
import im.actor.model.util.AtomicLongCompat;
import im.actor.model.util.ThreadLocalCompat;

/**
 * Created by ex3ndr on 16.02.15.
 */
public class JavaThreadingProvider implements ThreadingRuntime {

    public JavaThreadingProvider() {

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
        return new JavaAtomicInteger(value);
    }

    @Override
    public AtomicLongCompat createAtomicLong(long value) {
        return new JavaAtomicLong(value);
    }

    @Override
    public <T> ThreadLocalCompat<T> createThreadLocal() {
        return new JavaThreadLocal<T>();
    }

    @Override
    public AbsTimerCompat createTimer(Runnable runnable) {
        return new TimerCompat(runnable);
    }

    @Override
    public ActorDispatcher createDispatcher(String name, int threadsCount, ThreadPriority priority, ActorSystem actorSystem) {
        return new JavaDispatcherActor(name, actorSystem, threadsCount, priority);
    }

    @Override
    public ActorDispatcher createDefaultDispatcher(String name, ThreadPriority priority, ActorSystem actorSystem) {
        return createDispatcher(name, getCoresCount() * 2, priority, actorSystem);
    }
}
