package im.actor.core.js.providers.webrtc;

public interface JsMediaCallback {
    void onCreated(JsMediaStream mediaStream);

    void onError(JsUserMediaError error);
}