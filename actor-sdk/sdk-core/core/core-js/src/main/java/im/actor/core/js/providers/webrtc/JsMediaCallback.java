package im.actor.core.js.providers.webrtc;

public interface JsMediaCallback {
    void onCreated(JsUserMediaStream mediaStream);

    void onError(JsUserMediaError error);
}