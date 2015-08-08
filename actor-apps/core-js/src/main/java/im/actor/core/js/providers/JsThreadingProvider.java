/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.providers;

import java.util.Date;

import im.actor.core.concurrency.AbsTimerCompat;
import im.actor.core.js.providers.threading.JsAtomicInteger;
import im.actor.core.js.providers.threading.JsAtomicLong;
import im.actor.core.js.providers.threading.JsDispatch;
import im.actor.core.js.providers.threading.JsThreadLocal;
import im.actor.core.ThreadingProvider;
import im.actor.core.droidkit.actors.ActorSystem;
import im.actor.core.droidkit.actors.ThreadPriority;
import im.actor.core.droidkit.actors.mailbox.ActorDispatcher;
import im.actor.core.js.providers.threading.JsTimerCompat;
import im.actor.core.util.AtomicIntegerCompat;
import im.actor.core.util.AtomicLongCompat;
import im.actor.core.util.ThreadLocalCompat;

public class JsThreadingProvider implements ThreadingProvider {
    @Override
    public long getActorTime() {
        // TODO: Better approach
        return getCurrentTime();
    }

    @Override
    public long getCurrentTime() {
        return new Date().getTime();
    }

    @Override
    public long getSyncedCurrentTime() {
        return getCurrentTime();
    }

    @Override
    public int getCoresCount() {
        return 1;
    }

    @Override
    public AtomicIntegerCompat createAtomicInt(int value) {
        return new JsAtomicInteger(value);
    }

    @Override
    public AtomicLongCompat createAtomicLong(long value) {
        return new JsAtomicLong(value);
    }

    @Override
    public <T> ThreadLocalCompat<T> createThreadLocal() {
        return new JsThreadLocal<T>();
    }

    @Override
    public AbsTimerCompat createTimer(Runnable runnable) {
        return new JsTimerCompat(runnable);
    }

    @Override
    public ActorDispatcher createDispatcher(String name, int threadsCount, ThreadPriority priority, ActorSystem actorSystem) {
        return createDefaultDispatcher(name, priority, actorSystem);
    }

    @Override
    public ActorDispatcher createDefaultDispatcher(String name, ThreadPriority priority, ActorSystem system) {
        return new JsDispatch(name, system);
    }
}
