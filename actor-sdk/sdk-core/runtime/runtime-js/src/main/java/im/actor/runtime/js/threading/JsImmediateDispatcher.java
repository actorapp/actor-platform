package im.actor.runtime.js.threading;

import java.util.LinkedList;

import im.actor.runtime.threading.ImmediateDispatcher;

public class JsImmediateDispatcher implements ImmediateDispatcher {

    private static final int ITERATION_COUNT_MAX = 10;

    private final String name;
    private final JsSecureInterval secureInterval;
    private final LinkedList<Runnable> queue = new LinkedList<>();
    private boolean isInvalidated = false;

    public JsImmediateDispatcher(boolean allowWebWorker, String name) {
        this.name = name;
        this.secureInterval = JsSecureInterval.create(allowWebWorker, new Runnable() {
            @Override
            public void run() {
                isInvalidated = true;
                int iteration = 0;
                while (iteration < ITERATION_COUNT_MAX) {
                    if (queue.size() > 0) {
                        try {
                            queue.remove(0).run();
                        } catch (Throwable t) {
                            t.printStackTrace();
                            // Just ignore errors
                        }
                    } else {
                        break;
                    }
                    iteration++;
                }

                if (queue.size() > 0) {
                    secureInterval.scheduleNow();
                } else {
                    isInvalidated = false;
                }
            }
        });
        this.isInvalidated = true;
        this.secureInterval.scheduleNow();
    }

    @Override
    public void dispatchNow(Runnable runnable) {
        queue.add(runnable);

        if (!isInvalidated) {
            isInvalidated = true;
            secureInterval.scheduleNow();
        }
    }
}
