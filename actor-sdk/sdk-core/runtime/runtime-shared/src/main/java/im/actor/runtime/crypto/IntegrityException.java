package im.actor.runtime.crypto;

import java.io.IOException;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class IntegrityException extends IOException {
    public IntegrityException() {
    }

    public IntegrityException(String message) {
        super(message);
    }

    public IntegrityException(String message, Throwable cause) {
        super(message, cause);
    }

    public IntegrityException(Throwable cause) {
        super(cause);
    }
}
