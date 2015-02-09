package com.droidkit.actors.conf;

import com.droidkit.actors.ActorSystem;
import com.droidkit.actors.ThreadPriority;
import com.droidkit.actors.mailbox.ActorDispatcher;
import com.droidkit.actors.utils.AtomicIntegerCompat;
import com.droidkit.actors.utils.AtomicLongCompat;
import com.droidkit.actors.utils.ThreadLocalCompat;

/**
 * Created by ex3ndr on 06.02.15.
 */
public class EnvConfig {

    private EnvConfig() {
    }

    private static DispatcherFactory dispatcherFactory;
    private static JavaFactory javaFactory;

    public static JavaFactory getJavaFactory() {
        return javaFactory;
    }

    public static void setJavaFactory(JavaFactory javaFactory) {
        EnvConfig.javaFactory = javaFactory;
    }

    public static DispatcherFactory getDispatcherFactory() {
        return dispatcherFactory;
    }

    public static void setDispatcherFactory(DispatcherFactory dispatcherFactory) {
        EnvConfig.dispatcherFactory = dispatcherFactory;
    }

    public static ActorDispatcher createDispatcher(String name, int threadsCount, ThreadPriority priority, ActorSystem actorSystem) {
        if (dispatcherFactory == null) {
            throw new RuntimeException("EnvConfig not inited!");
        }
        return dispatcherFactory.createDispatcher(name, threadsCount, priority, actorSystem);
    }


    public static AtomicIntegerCompat createAtomicInt(int init) {
        if (javaFactory == null) {
            throw new RuntimeException("EnvConfig not inited!");
        }
        return javaFactory.createAtomicInt(init);
    }

    public static AtomicLongCompat createAtomicLong(long init) {
        if (javaFactory == null) {
            throw new RuntimeException("EnvConfig not inited!");
        }
        return javaFactory.createAtomicLong(init);
    }

    public static <T> ThreadLocalCompat<T> createThreadLocal() {
        if (javaFactory == null) {
            throw new RuntimeException("EnvConfig not inited!");
        }
        return javaFactory.createThreadLocal();
    }
}
