package im.actor.core.js.providers.webrtc;

import com.google.gwt.core.client.JavaScriptObject;

import im.actor.core.js.modules.JsScheduller;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.PromiseResolver;

public class JsPeerConnection extends JavaScriptObject {

    public static native JsPeerConnection create(JsPeerConnectionConfig config)/*-{
        return {peerConnection: new webkitRTCPeerConnection(config)};
    }-*/;

    protected JsPeerConnection() {

    }

    public final native void setListener(JsPeerConnectionListener listener)/*-{
        this.peerConnection.onicecandidate = function(candidate) {
            callback.@im.actor.core.js.providers.webrtc.JsPeerConnectionListener::onIceCandidate(*)(candidate);
        };
    }-*/;

    public final native void setLocalDescription(JsSessionDescription description)/*-{
        this.peerConnection.setLocalDescription(description);
    }-*/;

    public final native void setRemoteDescription(JsSessionDescription description)/*-{
        this.peerConnection.setRemoteDescription(description);
    }-*/;

    public final Promise<JsSessionDescription> createOffer() {
        return new Promise<>(new PromiseFunc<JsSessionDescription>() {
            @Override
            public void exec(final PromiseResolver<JsSessionDescription> resolver) {
                createOffer(new JsSessionDescriptionCallback() {
                    @Override
                    public void onOfferCreated(JsSessionDescription offer) {
                        resolver.result(offer);
                    }

                    @Override
                    public void onOfferFailure() {
                        resolver.error(new RuntimeException("Offer failure"));
                    }
                });
            }
        }).done(JsScheduller.scheduller());
    }

    private final native void createOffer(JsSessionDescriptionCallback callback)/*-{
        this.peerConnection.createOffer(function(offer) {
            callback.@im.actor.core.js.providers.webrtc.JsSessionDescriptionCallback::onOfferCreated(*)(offer);
        }, function(error) {
            callback.@im.actor.core.js.providers.webrtc.JsSessionDescriptionCallback::onOfferFailure(*)();
        });
    }-*/;
}
