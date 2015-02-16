package im.actor.model.droidkit.actors;

import im.actor.model.Threading;
import im.actor.model.droidkit.actors.mailbox.ActorDispatcher;
import im.actor.model.util.AtomicIntegerCompat;
import im.actor.model.util.AtomicLongCompat;
import im.actor.model.util.ThreadLocalCompat;

/**
 * Created by ex3ndr on 16.02.15.
 */
public class Environment {
    private static volatile Threading threading;

    public static void setThreading(Threading threading) {
        Environment.threading = threading;
    }

    public static ActorDispatcher createDefaultDispatcher(String name, ThreadPriority priority, ActorSystem actorSystem) {
        if (threading == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threading.createDefaultDispatcher(name, priority, actorSystem);
    }

    public static ActorDispatcher createDispatcher(String name, int threadsCount, ThreadPriority priority, ActorSystem actorSystem) {
        if (threading == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threading.createDispatcher(name, threadsCount, priority, actorSystem);
    }

    public static long getActorTime() {
        if (threading == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threading.getActorTime();
    }

    public static long getCurrentTime() {
        if (threading == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threading.getCurrentTime();
    }

    public static AtomicIntegerCompat createAtomicInt(int init) {
        if (threading == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threading.createAtomicInt(init);
    }

    public static AtomicLongCompat createAtomicLong(long init) {
        if (threading == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threading.createAtomicLong(init);
    }

    public static <T> ThreadLocalCompat<T> createThreadLocal() {
        if (threading == null) {
            throw new RuntimeException("Environment is not inited!");
        }
        return threading.createThreadLocal();
    }
}
