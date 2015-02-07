package com.droidkit.actors;

/**
 * Creator of custom actors
 *
 * @author Steve Ex3NDR Korshakov (steve@actor.im)
 */
public interface ActorCreator<T extends Actor> {
    /**
     * Create actor
     *
     * @return Actor
     */
    public T create();
}
