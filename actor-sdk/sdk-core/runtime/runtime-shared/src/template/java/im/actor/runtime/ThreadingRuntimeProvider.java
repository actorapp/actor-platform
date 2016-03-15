package im.actor.runtime;

import im.actor.runtime.actors.ThreadPriority;
import im.actor.runtime.threading.AtomicIntegerCompat;
import im.actor.runtime.threading.AtomicLongCompat;
import im.actor.runtime.threading.Dispatcher;
import im.actor.runtime.threading.ImmediateDispatcher;
import im.actor.runtime.threading.ThreadLocalCompat;
import im.actor.runtime.threading.WeakReferenceCompat;

public class ThreadingRuntimeProvider implements ThreadingRuntime {

    @Override
    public long getActorTime() {
        throw new RuntimeException("Dumb");
    }

    @Override
    public long getCurrentTime() {
        throw new RuntimeException("Dumb");
    }

    @Override
    public long getSyncedCurrentTime() {
        throw new RuntimeException("Dumb");
    }

    @Override
    public int getCoresCount() {
        throw new RuntimeException("Dumb");
    }

    @Override
    public AtomicIntegerCompat createAtomicInt(int value) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public AtomicLongCompat createAtomicLong(long value) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public <T> ThreadLocalCompat<T> createThreadLocal() {
        throw new RuntimeException("Dumb");
    }

    @Override
    public <T> WeakReferenceCompat<T> createWeakReference(T val) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public Dispatcher createDispatcher(String name) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public ImmediateDispatcher createImmediateDispatcher(String name, ThreadPriority priority) {
        throw new RuntimeException("Dumb");
    }
}
