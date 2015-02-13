package com.droidkit.engine._internal;

import com.droidkit.actors.Actor;

/**
 * Created by ex3ndr on 28.08.14.
 */
public class RunnableActor extends Actor {
    @Override
    public void onReceive(Object message) {
        if (message instanceof Runnable) {
            ((Runnable) message).run();
        }
    }
}
