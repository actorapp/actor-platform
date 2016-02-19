package im.actor.core.modules.calls.entity;

public class InvalidTransactionException extends Exception {

    public InvalidTransactionException() {
    }

    public InvalidTransactionException(String detailMessage) {
        super(detailMessage);
    }

    public InvalidTransactionException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public InvalidTransactionException(Throwable throwable) {
        super(throwable);
    }
}
