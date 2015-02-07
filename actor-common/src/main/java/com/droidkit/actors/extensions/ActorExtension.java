package com.droidkit.actors.extensions;

/**
 * Created by ex3ndr on 06.09.14.
 */
public interface ActorExtension {
    public void preStart();

    public boolean onReceive(Object message);

    public void postStop();
}
