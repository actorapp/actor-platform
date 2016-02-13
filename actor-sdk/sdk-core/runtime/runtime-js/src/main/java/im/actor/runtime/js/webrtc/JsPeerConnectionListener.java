package im.actor.runtime.js.webrtc;

public interface JsPeerConnectionListener {
    void onIceCandidate(JsRTCIceCandidate candidate);

    void onStreamAdded(JsMediaStream stream);

    void onRenegotiationNeeded();

    void onStreamRemoved(JsMediaStream stream);
}