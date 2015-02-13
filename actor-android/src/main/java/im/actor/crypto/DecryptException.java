package im.actor.crypto;

/**
 * Created by ex3ndr on 19.10.14.
 */
public class DecryptException extends Exception {
    public DecryptException() {
    }

    public DecryptException(String detailMessage) {
        super(detailMessage);
    }

    public DecryptException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DecryptException(Throwable throwable) {
        super(throwable);
    }
}
