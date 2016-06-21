package im.actor.runtime.js.webrtc.js;

public interface JsMediaCallback {
    void onCreated(JsMediaStream mediaStream);

    void onError(JsUserMediaError error);
}