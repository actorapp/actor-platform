package im.actor.core.js.providers.webrtc;

public interface JsSessionDescriptionCallback {
    void onOfferCreated(JsSessionDescription offer);

    void onOfferFailure();
}
