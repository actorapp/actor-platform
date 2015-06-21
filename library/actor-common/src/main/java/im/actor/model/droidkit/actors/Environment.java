/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.droidkit.actors;

import im.actor.model.DispatcherProvider;
import im.actor.model.ThreadingProvider;
import im.actor.model.concurrency.AbsTimerCompat;
import im.actor.model.droidkit.actors.mailbox.ActorDispatcher;
import im.actor.model.util.AtomicIntegerCompat;
import im.actor.model.util.AtomicLongCompat;
import im.actor.model.util.ThreadLocalCompat;

public class Environment {
    private static volatile ThreadingProvider threadingProvider;
    private static volatile DispatcherProvider dispatcherProvider;

    public static void setThreadingProvider(ThreadingProvider threadingProvider) {
        Environment.threadingProvider = threadingProvider;
    }

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
        if (threadingProvider == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threadingProvider.createDefaultDispatcher(name, priority, actorSystem);
    }

    public static ActorDispatcher createDispatcher(String name, int threadsCount, ThreadPriority priority, ActorSystem actorSystem) {
        if (threadingProvider == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threadingProvider.createDispatcher(name, threadsCount, priority, actorSystem);
    }

    public static long getActorTime() {
        if (threadingProvider == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threadingProvider.getActorTime();
    }

    public static long getCurrentTime() {
        if (threadingProvider == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threadingProvider.getCurrentTime();
    }

    public static long getCurrentSyncedTime() {
        if (threadingProvider == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threadingProvider.getSyncedCurrentTime();
    }

    public static AtomicIntegerCompat createAtomicInt(int init) {
        if (threadingProvider == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threadingProvider.createAtomicInt(init);
    }

    public static AtomicLongCompat createAtomicLong(long init) {
        if (threadingProvider == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threadingProvider.createAtomicLong(init);
    }

    public static <T> ThreadLocalCompat<T> createThreadLocal() {
        if (threadingProvider == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threadingProvider.createThreadLocal();
    }

    public static AbsTimerCompat createTimer(Runnable runnable) {
        if (threadingProvider == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threadingProvider.createTimer(runnable);
    }
}
