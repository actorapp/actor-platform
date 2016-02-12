package im.actor.runtime.generic.threading;

import java.util.ArrayList;

import im.actor.runtime.actors.ThreadPriority;
import im.actor.runtime.threading.ImmediateDispatcher;

public class GenericImmediateDispatcher implements ImmediateDispatcher {

    boolean isClosed = false;
    private final Thread workingThread;
    private final ArrayList<Runnable> queue = new ArrayList<>();

    public GenericImmediateDispatcher(String name, ThreadPriority priority) {
        this.workingThread = new DispatcherThread();
        switch (priority) {
            case HIGH:
                this.workingThread.setPriority(Thread.MAX_PRIORITY);
            case LOW:
                this.workingThread.setPriority(Thread.MIN_PRIORITY);
            default:
            case NORMAL:
                this.workingThread.setPriority(Thread.NORM_PRIORITY);
        }
        this.workingThread.setName(name);
        this.workingThread.start();
    }

    @Override
    public void dispatchNow(Runnable runnable) {
        synchronized (queue) {
            queue.add(runnable);
            queue.notifyAll();
        }
    }


    /**
     * Thread class for dispatching
     */
    private class DispatcherThread extends Thread {
        @Override
        public void run() {
            while (!isClosed) {
                Runnable pending = null;
                synchronized (queue) {
                    if (queue.size() > 0) {
                        pending = queue.remove(0);
                    }
                    if (pending == null) {
                        try {
                            queue.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                }

                if (pending != null) {
                    try {
                        pending.run();
                    } catch (Throwable t) {
                        // Possibly danger situation, but i hope this will not corrupt JVM
                        // For example: on Android we could always continue execution after OutOfMemoryError
                        // Anyway, better to catch all errors manually in runnable
                        t.printStackTrace();
                    }
                }
            }
        }
    }
}
