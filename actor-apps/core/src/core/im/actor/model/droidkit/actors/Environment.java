/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.droidkit.actors;

import im.actor.model.DispatcherProvider;
import im.actor.model.runtime.ThreadingRuntime;
import im.actor.model.concurrency.AbsTimerCompat;
import im.actor.model.droidkit.actors.mailbox.ActorDispatcher;
import im.actor.model.runtime.ThreadingRuntimeProvider;
import im.actor.model.util.AtomicIntegerCompat;
import im.actor.model.util.AtomicLongCompat;
import im.actor.model.util.ThreadLocalCompat;

public class Environment {

    private static volatile ThreadingRuntime threadingRuntime = new ThreadingRuntimeProvider();

    private static volatile DispatcherProvider dispatcherProvider;

    public static void setDispatcherProvider(DispatcherProvider dispatcherProvider) {
        Environment.dispatcherProvider = dispatcherProvider;
    }

    public static void dispatchCallback(Runnable runnable) {
        if (dispatcherProvider == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        dispatcherProvider.dispatch(runnable);
    }

    public static ActorDispatcher createDefaultDispatcher(String name, ThreadPriority priority, ActorSystem actorSystem) {
        if (threadingRuntime == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threadingRuntime.createDefaultDispatcher(name, priority, actorSystem);
    }

    public static ActorDispatcher createDispatcher(String name, int threadsCount, ThreadPriority priority, ActorSystem actorSystem) {
        if (threadingRuntime == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threadingRuntime.createDispatcher(name, threadsCount, priority, actorSystem);
    }

    public static long getActorTime() {
        if (threadingRuntime == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threadingRuntime.getActorTime();
    }

    public static long getCurrentTime() {
        if (threadingRuntime == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threadingRuntime.getCurrentTime();
    }

    public static long getCurrentSyncedTime() {
        if (threadingRuntime == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threadingRuntime.getSyncedCurrentTime();
    }

    public static AtomicIntegerCompat createAtomicInt(int init) {
        if (threadingRuntime == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threadingRuntime.createAtomicInt(init);
    }

    public static AtomicLongCompat createAtomicLong(long init) {
        if (threadingRuntime == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threadingRuntime.createAtomicLong(init);
    }

    public static <T> ThreadLocalCompat<T> createThreadLocal() {
        if (threadingRuntime == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threadingRuntime.createThreadLocal();
    }

    public static AbsTimerCompat createTimer(Runnable runnable) {
        if (threadingRuntime == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threadingRuntime.createTimer(runnable);
    }
}
