/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors;

/**
 * Time used by actor system, uses System.nanoTime() inside
 */
public class ActorTime {
    /**
     * Getting current actor system time
     *
     * @return actor system time
     */
    public static long currentTime() {
        return im.actor.runtime.Runtime.getActorTime();
    }
}