package im.actor.runtime.generic.threading;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import im.actor.runtime.Runtime;
import im.actor.runtime.actors.ThreadPriority;
import im.actor.runtime.threading.ImmediateDispatcher;

public class GenericImmediateDispatcher implements ImmediateDispatcher {

    private Executor EXECUTOR;
    private boolean isInited;

    public GenericImmediateDispatcher(String name, ThreadPriority priority) {
        Runtime.dispatch(() -> {
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
            isInited = true;
        });
    }

    @Override
    public synchronized void dispatchNow(Runnable runnable) {
        if (EXECUTOR == null) {
            EXECUTOR = Executors.newSingleThreadExecutor();
        }
        if (isInited) {
            EXECUTOR.execute(runnable);
        } else {
            Runtime.dispatch(() -> EXECUTOR.execute(runnable));
        }
    }
}
