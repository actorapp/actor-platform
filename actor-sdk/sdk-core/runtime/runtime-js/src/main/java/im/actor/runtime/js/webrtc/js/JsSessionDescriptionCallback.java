package im.actor.runtime.js.webrtc.js;

public interface JsSessionDescriptionCallback {
    void onOfferCreated(JsSessionDescription offer);

    void onOfferFailure();
}
