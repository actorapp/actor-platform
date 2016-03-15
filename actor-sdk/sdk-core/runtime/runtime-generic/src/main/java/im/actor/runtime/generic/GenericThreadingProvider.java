package im.actor.runtime.generic;

import im.actor.runtime.generic.threading.GenericAtomicInteger;
import im.actor.runtime.generic.threading.GenericAtomicLong;
import im.actor.runtime.generic.threading.GenericImmediateDispatcher;
import im.actor.runtime.generic.threading.GenericThreadLocal;
import im.actor.runtime.ThreadingRuntime;
import im.actor.runtime.actors.ThreadPriority;
import im.actor.runtime.generic.threading.GenericWeakReference;
import im.actor.runtime.threading.AtomicIntegerCompat;
import im.actor.runtime.threading.AtomicLongCompat;
import im.actor.runtime.threading.ImmediateDispatcher;
import im.actor.runtime.threading.ThreadLocalCompat;
import im.actor.runtime.threading.WeakReferenceCompat;

public abstract class GenericThreadingProvider implements ThreadingRuntime {

    public GenericThreadingProvider() {

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
        return new GenericAtomicInteger(value);
    }

    @Override
    public <T> WeakReferenceCompat<T> createWeakReference(T val) {
        return new GenericWeakReference<>(val);
    }

    @Override
    public AtomicLongCompat createAtomicLong(long value) {
        return new GenericAtomicLong(value);
    }

    @Override
    public <T> ThreadLocalCompat<T> createThreadLocal() {
        return new GenericThreadLocal<>();
    }

    @Override
    public ImmediateDispatcher createImmediateDispatcher(String name, ThreadPriority priority) {
        return new GenericImmediateDispatcher(name, priority);
    }
}
