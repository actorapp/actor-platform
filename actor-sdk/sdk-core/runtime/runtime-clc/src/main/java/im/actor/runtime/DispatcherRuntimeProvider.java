package im.actor.runtime;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/**
 * Created by ex3ndr on 07.08.15.
 */
public class DispatcherRuntimeProvider implements DispatcherRuntime {

    @Override
    public void dispatch(Runnable runnable) {
        new Thread(runnable).start();
    }
}
