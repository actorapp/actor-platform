package im.actor.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Created by ex3ndr on 07.08.15.
 */

public class MainThreadRuntimeProvider implements MainThreadRuntime {

    private static final Logger logger = LoggerFactory.getLogger(MainThreadRuntimeProvider.class);


    public static final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
    public  Thread main;

    public MainThreadRuntimeProvider() {
        main = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true)
                {
                    try {
                        queue.take().run();
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage(),e);
                    }
                }
            }
        });

        main.start();
    }


    @Override
    public void postToMainThread(Runnable runnable) {
        queue.add(runnable);
    }

    @Override
    public boolean isMainThread() {
        boolean isMain = Thread.currentThread() == main;
        return isMain;
    }

    @Override
    public boolean isSingleThread() {
        return false;
    }
}
