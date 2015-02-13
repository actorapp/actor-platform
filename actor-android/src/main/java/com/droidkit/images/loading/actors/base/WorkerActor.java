package com.droidkit.images.loading.actors.base;

import com.droidkit.actors.dispatch.RunnableDispatcher;
import com.droidkit.actors.tasks.TaskActor;

/**
 * Created by ex3ndr on 27.08.14.
 */
public abstract class WorkerActor<T> extends TaskActor<T> {

    private RunnableDispatcher dispatcher;
    private WorkerRunnable runnable;

    public WorkerActor(RunnableDispatcher dispatcher) {
        this.dispatcher = dispatcher;
        this.runnable = new WorkerRunnable();
    }

    protected abstract T doWork() throws Exception;

    @Override
    public void startTask() {
        doStartTask();
    }

    protected final void doStartTask() {
        dispatcher.postAction(runnable);
    }

    @Override
    public void onTaskObsolete() {
        dispatcher.removeAction(runnable);
    }

    private class WorkerRunnable implements Runnable {
        @Override
        public void run() {
            try {
                T res = doWork();
                complete(res);
            } catch (Throwable t) {
                error(t);
            }
        }
    }
}
