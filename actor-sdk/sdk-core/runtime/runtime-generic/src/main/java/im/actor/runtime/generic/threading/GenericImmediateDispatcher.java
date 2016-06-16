package im.actor.runtime.generic.threading;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import im.actor.runtime.Runtime;
import im.actor.runtime.actors.ThreadPriority;
import im.actor.runtime.threading.ImmediateDispatcher;

public class GenericImmediateDispatcher implements ImmediateDispatcher {

    private final Executor EXECUTOR;

    public GenericImmediateDispatcher(String name, ThreadPriority priority) {
        EXECUTOR = Executors.newSingleThreadExecutor(r -> {
            Thread workingThread = new Thread(r);
            switch (priority) {
                case HIGH:
                    workingThread.setPriority(Thread.MAX_PRIORITY);
                case LOW:
                    workingThread.setPriority(Thread.MIN_PRIORITY);
                default:
                case NORMAL:
                    workingThread.setPriority(Thread.NORM_PRIORITY);
            }
            workingThread.setName(name);
            return workingThread;
        });
    }

    @Override
    public synchronized void dispatchNow(Runnable runnable) {
        EXECUTOR.execute(runnable);
    }
}
