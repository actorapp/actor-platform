package im.actor.model.droidkit.actors;

import im.actor.model.ThreadingProvider;
import im.actor.model.droidkit.actors.mailbox.ActorDispatcher;
import im.actor.model.util.AtomicIntegerCompat;
import im.actor.model.util.AtomicLongCompat;
import im.actor.model.util.ThreadLocalCompat;

/**
 * Created by ex3ndr on 16.02.15.
 */
public class Environment {
    private static volatile ThreadingProvider threadingProvider;

    public static void setThreadingProvider(ThreadingProvider threadingProvider) {
        Environment.threadingProvider = threadingProvider;
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
}
