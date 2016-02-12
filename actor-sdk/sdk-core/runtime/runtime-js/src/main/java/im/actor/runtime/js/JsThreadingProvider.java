/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js;

import java.util.Date;

import im.actor.runtime.ThreadingRuntime;
import im.actor.runtime.actors.ThreadPriority;
import im.actor.runtime.js.threading.JsAtomicInteger;
import im.actor.runtime.js.threading.JsAtomicLong;
import im.actor.runtime.js.threading.JsDispatcher;
import im.actor.runtime.js.threading.JsImmediateDispatcher;
import im.actor.runtime.js.threading.JsThreadLocal;
import im.actor.runtime.threading.AtomicIntegerCompat;
import im.actor.runtime.threading.AtomicLongCompat;
import im.actor.runtime.threading.Dispatcher;
import im.actor.runtime.threading.ImmediateDispatcher;
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
        return new JsThreadLocal<>();
    }

    @Override
    public Dispatcher createDispatcher(String name) {
        return new JsDispatcher();
    }

    @Override
    public ImmediateDispatcher createImmediateDispatcher(String name, ThreadPriority priority) {
        return new JsImmediateDispatcher(name);
    }
}
