/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors.messages;

/**
 * DeadLetter sent whet message was not processed by target actor
 */
public class DeadLetter {
    private Object message;

    public DeadLetter(Object message) {
        this.message = message;
    }

    public Object getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "DeadLetter(" + message + ")";
    }
}
