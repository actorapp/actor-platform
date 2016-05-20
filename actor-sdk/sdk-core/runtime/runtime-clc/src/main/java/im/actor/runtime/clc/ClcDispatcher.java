package im.actor.runtime.clc;

import im.actor.runtime.threading.DispatchCancel;
import im.actor.runtime.threading.Dispatcher;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by elenoon on 3/1/16.
 */
public class ClcDispatcher implements Dispatcher {

    final ScheduledThreadPoolExecutor executor;
    public ClcDispatcher(String name){
         executor = new ScheduledThreadPoolExecutor(5);
    }

    @Override
    public DispatchCancel dispatch(Runnable message, long delay) {
        executor.schedule(message, delay, TimeUnit.MILLISECONDS);
        return null;
    }
}
