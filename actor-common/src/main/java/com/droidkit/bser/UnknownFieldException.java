package com.droidkit.bser;

/**
 * Created by ex3ndr on 18.10.14.
 */
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