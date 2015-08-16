/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.generic.threading;

import java.util.concurrent.atomic.AtomicInteger;

import im.actor.runtime.actors.ThreadPriority;
import im.actor.runtime.actors.dispatch.AbstractDispatchQueue;
import im.actor.runtime.actors.dispatch.AbstractDispatcher;
import im.actor.runtime.actors.dispatch.Dispatch;
import im.actor.runtime.actors.dispatch.DispatchResult;

import static im.actor.runtime.actors.ActorTime.currentTime;

/**
 * ThreadPoolDispatcher is used for dispatching messages on it's own threads.
 * Class is completely thread-safe.
 */
public class GenericDispatcherThreads<T, Q extends AbstractDispatchQueue<T>> extends AbstractDispatcher<T, Q> {

    private static final AtomicInteger INDEX = new AtomicInteger(1);

    private Thread[] threads;

    private final int count;
    private final ThreadPriority priority;

    private boolean isClosed = false;

    private final int id;

    private final String name;

    /**
     * Dispatcher constructor
     *
     * @param count    thread count
     * @param priority thread priority
     * @param queue    queue for messages
     *                 (see {@link AbstractDispatchQueue} for more information)
     * @param dispatch Dispatch for message processing
     */
    public GenericDispatcherThreads(String name, int count, ThreadPriority priority, final Q queue, Dispatch<T> dispatch, boolean createThreads) {
        super(queue, dispatch);

        this.id = INDEX.getAndIncrement();
        this.name = name;
        this.count = count;
        this.priority = priority;

        if (createThreads) {
            startPool();
        }
    }

    public void startPool() {
        if (this.threads != null) {
            return;
        }
        this.threads = new Thread[count];
        for (int i = 0; i < count; i++) {
            this.threads[i] = new DispatcherThread();
            this.threads[i].setName("Pool_" + name + "_" + i);
            switch (priority) {
                case HIGH:
                    this.threads[i].setPriority(Thread.MAX_PRIORITY);
                    break;
                case LOW:
                    this.threads[i].setPriority(Thread.MIN_PRIORITY);
                    break;
                default:
                case NORMAL:
                    this.threads[i].setPriority(Thread.NORM_PRIORITY);
                    break;
            }
            this.threads[i].start();
        }
    }

    /**
     * Closing of dispatcher no one actions will be executed after calling this method.
     */
    public void close() {
        isClosed = true;
        notifyDispatcher();
    }

    /**
     * Notification about queue change
     */
    @Override
    protected void notifyDispatcher() {
        if (threads != null) {
            synchronized (threads) {
                threads.notifyAll();
                for (Thread thread : threads) {
                    ((DispatcherThread) thread).setChanged(true);
                }
            }
        }
    }

    /**
     * Thread class for dispatching
     */
    private class DispatcherThread extends Thread {

        private boolean isChanged = false;

        public boolean isChanged() {
            return isChanged;
        }

        public void setChanged(boolean isChanged) {
            this.isChanged = isChanged;
        }

        @Override
        public void run() {
            while (!isClosed) {
                long time = currentTime();

                synchronized (threads) {
                    isChanged = false;
                }

                final DispatchResult action = getQueue().dispatch(time);

                if (!action.isResult()) {
                    if (isChanged) {
                        continue;
                    }

                    synchronized (threads) {
                        long delay = action.getDelay();
                        action.recycle();

                        try {
                            if (delay > 0) {
                                threads.wait(delay);
                            }
                            continue;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                }

                try {
                    T actiondData = (T) action.getRes();
                    action.recycle();
                    dispatchMessage(actiondData);
                } catch (Throwable t) {
                    // Possibly danger situation, but i hope this will not corrupt JVM
                    // For example: on Android we could always continue execution after OutOfMemoryError
                    // Anyway, better to catch all errors manually in dispatchMessage
                    t.printStackTrace();
                }
            }
        }
    }
}