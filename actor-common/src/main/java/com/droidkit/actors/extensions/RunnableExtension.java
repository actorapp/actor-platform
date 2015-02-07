package com.droidkit.actors.extensions;

/**
 * Created by ex3ndr on 11.09.14.
 */
public class RunnableExtension implements ActorExtension {
    @Override
    public void preStart() {

    }

    @Override
    public boolean onReceive(Object message) {
        if (message instanceof Runnable) {
            ((Runnable) message).run();
            return true;
        }
        return false;
    }

    @Override
    public void postStop() {

    }
}
