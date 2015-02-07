package com.droidkit.actors.messages;

/**
 * Message for starting actors
 *
 * @author Stepan Ex3NDR Korshakov (me@ex3ndr.com)
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
