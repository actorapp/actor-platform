package im.actor.runtime.http;

public class HTTPError extends Exception {

    private int errorCode;
    private int retryInSecs;

    public HTTPError(int errorCode, int retryInSecs) {
        this.errorCode = errorCode;
        this.retryInSecs = retryInSecs;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public int getRetryInSecs() {
        return retryInSecs;
    }
}
