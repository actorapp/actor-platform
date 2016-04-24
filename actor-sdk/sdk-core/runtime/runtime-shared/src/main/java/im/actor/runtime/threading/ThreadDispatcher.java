package im.actor.runtime.threading;

import java.util.ArrayList;

import im.actor.runtime.Runtime;

public class ThreadDispatcher {

    private static final ThreadLocalCompat<ArrayList<SimpleDispatcher>> currentDispatcher = Runtime.createThreadLocal();

    public static void pushDispatcher(SimpleDispatcher dispatcher) {
        if (currentDispatcher.get() == null) {
            ArrayList<SimpleDispatcher> dispatchers = new ArrayList<>();
            dispatchers.add(dispatcher);
            currentDispatcher.set(dispatchers);
        } else {
            currentDispatcher.get().add(dispatcher);
        }
    }

    public static void popDispatcher() {
        ArrayList<SimpleDispatcher> dispatchers = currentDispatcher.get();
        if (dispatchers == null || dispatchers.size() == 0) {
            throw new RuntimeException("Current Thread doesn't have Active Dispatchers");
        } else {
            dispatchers.remove(dispatchers.size() - 1);
        }
    }

    public static SimpleDispatcher peekDispatcher() {
        ArrayList<SimpleDispatcher> dispatchers = currentDispatcher.get();
        if (dispatchers == null || dispatchers.size() == 0) {
            throw new RuntimeException("Current Thread doesn't have Active Dispatchers");
        } else {
            return dispatchers.get(dispatchers.size() - 1);
        }
    }

    public static void dispatchOnCurrentThread(Runnable runnable) {
        peekDispatcher().dispatch(runnable);
    }
}
