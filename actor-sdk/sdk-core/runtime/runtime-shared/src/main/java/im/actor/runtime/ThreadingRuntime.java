/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.actors.ThreadPriority;
import im.actor.runtime.threading.Dispatcher;
import im.actor.runtime.threading.AtomicIntegerCompat;
import im.actor.runtime.threading.AtomicLongCompat;
import im.actor.runtime.threading.ImmediateDispatcher;
import im.actor.runtime.threading.ThreadLocalCompat;
import im.actor.runtime.threading.WeakReferenceCompat;

/**
 * Provider for multithreading support. Contains all required methods for performing asynchronous operations.
 */
public interface ThreadingRuntime {

    /**
     * Time in ms from some fixed point in time and that not relied to local time changes.
     *
     * @return time in ms
     */
    @ObjectiveCName("getActorTime")
    long getActorTime();

    /**
     * Current unix-time of system
     *
     * @return time in ms
     */
    @ObjectiveCName("getCurrentTime")
    long getCurrentTime();

    /**
     * Synchronized time by NTP. Used for more accurate timing of messages.
     * Return value from getCurrentTime() if not supported.
     *
     * @return time in ms
     */
    @ObjectiveCName("getSyncedCurrentTime")
    long getSyncedCurrentTime();

    /**
     * Number of computing cores in environment
     *
     * @return cores count
     */
    @ObjectiveCName("getCoresCount")
    int getCoresCount();

    /**
     * Creating compatible AtomicInteger object
     *
     * @param value initial value of AtomicInteger
     * @return the AtomicInteger
     */
    @ObjectiveCName("createAtomicIntWithInitValue:")
    AtomicIntegerCompat createAtomicInt(int value);

    /**
     * Creating compatible AtomicLong object
     *
     * @param value initial value of AtomicLong
     * @return the AtomicLong
     */
    @ObjectiveCName("createAtomicLongWithInitValue:")
    AtomicLongCompat createAtomicLong(long value);

    /**
     * Creating compatible ThreadLocal object
     *
     * @param <T> type of container
     * @return the ThreadLocal object
     */
    @ObjectiveCName("createThreadLocal")
    <T> ThreadLocalCompat<T> createThreadLocal();

    /**
     * Creating compatible weak reference
     *
     * @param val value for reference
     * @param <T> type of reference
     * @return weak reference
     */
    @ObjectiveCName("createWeakReference:")
    <T> WeakReferenceCompat<T> createWeakReference(T val);

    /**
     * Creating Dispatcher for very lightweight tasks
     *
     * @return the Dispatcher object
     */
    @ObjectiveCName("createDispatcherWithName:")
    Dispatcher createDispatcher(String name);

    /**
     * Creating of Actor Dispatcher for dispatching of actor's Envelopes
     *
     * @param name     Name of dispatcher
     * @param priority priority of dispatcher
     * @return created dispatcher
     */
    @ObjectiveCName("createImmediateDispatcherWithName:withPriority:")
    ImmediateDispatcher createImmediateDispatcher(String name, ThreadPriority priority);
}
