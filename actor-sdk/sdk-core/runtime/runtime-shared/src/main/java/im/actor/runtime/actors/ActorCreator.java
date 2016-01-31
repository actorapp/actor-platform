/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors;

/**
 * Creator of custom actors
 */
public interface ActorCreator {
    /**
     * Create actor
     *
     * @return Actor
     */
    Actor create();
}
