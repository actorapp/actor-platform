/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.bser;

import java.io.IOException;

public class UnknownFieldException extends IOException {
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