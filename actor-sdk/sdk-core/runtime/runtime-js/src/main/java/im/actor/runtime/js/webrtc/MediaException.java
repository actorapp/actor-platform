package im.actor.runtime.js.webrtc;

import im.actor.runtime.js.webrtc.js.JsUserMediaError;

public class MediaException extends RuntimeException {

    private JsUserMediaError error;

    public MediaException(JsUserMediaError error) {
        this.error = error;
    }

    public JsUserMediaError getError() {
        return error;
    }
}
