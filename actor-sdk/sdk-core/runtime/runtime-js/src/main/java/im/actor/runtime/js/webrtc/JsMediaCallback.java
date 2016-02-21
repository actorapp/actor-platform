package im.actor.runtime.js.webrtc;

public interface JsMediaCallback {
    void onCreated(JsMediaStream mediaStream);

    void onError(JsUserMediaError error);
}