/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.droidkit.bser;

public class UnknownFieldException extends RuntimeException {
    public UnknownFieldException() {
    }

    public UnknownFieldException(String message) {
        super(message);
    }

    public UnknownFieldException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownFieldException(Throwable cause) {
        super(cause);
    }
}