/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.bser;

public class IncorrectTypeException extends RuntimeException {
    public IncorrectTypeException() {
    }

    public IncorrectTypeException(String detailMessage) {
        super(detailMessage);
    }

    public IncorrectTypeException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public IncorrectTypeException(Throwable throwable) {
        super(throwable);
    }
}
