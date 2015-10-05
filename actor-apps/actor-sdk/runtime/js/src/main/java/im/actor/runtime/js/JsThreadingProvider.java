/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js;

import java.util.Date;

import im.actor.runtime.ThreadingRuntime;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.ThreadPriority;
import im.actor.runtime.actors.mailbox.ActorDispatcher;
import im.actor.runtime.js.threading.JsAtomicInteger;
import im.actor.runtime.js.threading.JsAtomicLong;
import im.actor.runtime.js.threading.JsDispatch;
import im.actor.runtime.js.threading.JsThreadLocal;
import im.actor.runtime.js.threading.JsTimerCompat;
import im.actor.runtime.threading.AbsTimerCompat;
import im.actor.runtime.threading.AtomicIntegerCompat;
import im.actor.runtime.threading.AtomicLongCompat;
import im.actor.runtime.threading.ThreadLocalCompat;

public class JsThreadingProvider implements ThreadingRuntime {

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
