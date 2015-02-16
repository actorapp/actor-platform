package im.actor.model.jvm;

import im.actor.model.Threading;
import im.actor.model.droidkit.actors.ActorSystem;
import im.actor.model.droidkit.actors.ThreadPriority;
import im.actor.model.droidkit.actors.mailbox.ActorDispatcher;
import im.actor.model.util.AtomicIntegerCompat;
import im.actor.model.util.AtomicLongCompat;
import im.actor.model.util.ThreadLocalCompat;
import im.actor.model.jvm.threads.JavaDispatcherActor;
import im.actor.model.jvm.threads.JavaAtomicInteger;
import im.actor.model.jvm.threads.JavaAtomicLong;
import im.actor.model.jvm.threads.JavaThreadLocal;

/**
 * Created by ex3ndr on 16.02.15.
 */
public class JavaThreading implements Threading {

    public JavaThreading() {

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
    public int getCoresCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    @Override
    public AtomicIntegerCompat createAtomicInt(int init) {
        return new JavaAtomicInteger(init);
    }

    @Override
    public AtomicLongCompat createAtomicLong(long init) {
        return new JavaAtomicLong(init);
    }

    @Override
    public <T> ThreadLocalCompat<T> createThreadLocal() {
        return new JavaThreadLocal<T>();
    }

    @Override
    public ActorDispatcher createDispatcher(String name, int threadsCount, ThreadPriority priority, ActorSystem actorSystem) {
        return new JavaDispatcherActor(name, actorSystem, threadsCount, priority);
    }

    @Override
    public ActorDispatcher createDefaultDispatcher(String name, ThreadPriority priority, ActorSystem actorSystem) {
        return createDispatcher(name, getCoresCount(), priority, actorSystem);
    }
}
