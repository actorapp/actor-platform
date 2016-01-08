package im.actor.crypto;

import java.io.IOException;

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
