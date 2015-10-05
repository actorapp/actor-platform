/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors.messages;

/**
 * Message for starting actors
 */
public final class StartActor {
    public static final StartActor INSTANCE = new StartActor();

    private StartActor() {
    }

    @Override
    public String toString() {
        return "StartActor";
    }
}
