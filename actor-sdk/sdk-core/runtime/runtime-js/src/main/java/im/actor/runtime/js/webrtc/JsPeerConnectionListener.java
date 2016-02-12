package im.actor.runtime.js.webrtc;

public interface JsPeerConnectionListener {
    void onIceCandidate(JsRTCIceCandidate candidate);

    void onIceCandidatesEnded();

    void onStreamAdded(JsMediaStream stream);
}