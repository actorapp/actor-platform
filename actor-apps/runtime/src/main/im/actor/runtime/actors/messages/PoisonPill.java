/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors.messages;

/**
 * PoisonPill message for killing actors
 */
public final class PoisonPill {
    public static final PoisonPill INSTANCE = new PoisonPill();

    private PoisonPill() {
    }

    @Override
    public String toString() {
        return "PoisonPill";
    }
}
