package im.actor.runtime.http;

public class HTTPError extends Exception {

    private int errorCode;

    public HTTPError(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
