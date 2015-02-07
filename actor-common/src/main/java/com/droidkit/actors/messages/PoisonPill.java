package com.droidkit.actors.messages;

/**
 * PoisonPill message for killing actors
 *
 * @author Stepan Ex3NDR Korshakov (me@ex3ndr.com)
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
