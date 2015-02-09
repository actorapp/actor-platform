package com.droidkit.bser;

/**
 * Created by ex3ndr on 17.10.14.
 */
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
