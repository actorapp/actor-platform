package im.actor.runtime.js.webrtc;

public interface JsSessionDescriptionCallback {
    void onOfferCreated(JsSessionDescription offer);

    void onOfferFailure();
}
