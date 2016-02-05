package im.actor.core.js.providers.webrtc;

public class MediaException extends RuntimeException {

    private JsUserMediaError error;

    public MediaException(JsUserMediaError error) {
        this.error = error;
    }

    public JsUserMediaError getError() {
        return error;
    }
}
