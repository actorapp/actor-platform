package im.actor.runtime.js.webrtc;

import com.google.gwt.core.client.JavaScriptObject;

import im.actor.runtime.js.entity.JsClosure;
import im.actor.runtime.js.entity.JsClosureError;

public class JsPeerConnection extends JavaScriptObject {

    public static native JsPeerConnection create(JsPeerConnectionConfig config)/*-{
        var peerConnectionClass = $wnd.RTCPeerConnection || $wnd.mozRTCPeerConnection ||
                       $wnd.webkitRTCPeerConnection || $wnd.msRTCPeerConnection;
        return {peerConnection: new peerConnectionClass(config)};
    }-*/;

    protected JsPeerConnection() {

    }

    public final native void addStream(JsMediaStream stream)/*-{
        this.peerConnection.addStream(stream);
    }-*/;

    public final native void addIceCandidate(int label, String candidate)/*-{
        this.peerConnection.addIceCandidate(new RTCIceCandidate({sdpMLineIndex: label, candidate: candidate}));
    }-*/;

    public final native void close()/*-{
        this.peerConnection.close();
    }-*/;

    public final native void createOffer(JsSessionDescriptionCallback callback)/*-{

        var sdpConstraints = {
            'mandatory': {
                'OfferToReceiveAudio': true,
                'OfferToReceiveVideo': false
            }
        };


        this.peerConnection.createOffer(function(offer) {
            callback.@im.actor.runtime.js.webrtc.JsSessionDescriptionCallback::onOfferCreated(*)(offer);
        }, function(error) {
        $wnd.console.warn(error);
            callback.@im.actor.runtime.js.webrtc.JsSessionDescriptionCallback::onOfferFailure(*)();
        }, sdpConstraints);
    }-*/;

    public final native void createAnswer(JsSessionDescriptionCallback callback)/*-{

        var sdpConstraints = {
            'mandatory': {
                'OfferToReceiveAudio': true,
                'OfferToReceiveVideo': false
            }
        };

        this.peerConnection.createAnswer(function(offer) {
            callback.@im.actor.runtime.js.webrtc.JsSessionDescriptionCallback::onOfferCreated(*)(offer);
        }, function(error) {
            $wnd.console.warn(error);
            callback.@im.actor.runtime.js.webrtc.JsSessionDescriptionCallback::onOfferFailure(*)();
        }, sdpConstraints);
    }-*/;

    public final native void setRemoteDescription(JsSessionDescription description, JsClosure closure, JsClosureError error)/*-{
        this.peerConnection.setRemoteDescription(description, function() {
            closure.@im.actor.runtime.js.entity.JsClosure::callback(*)();
        }, function(e) {
            $wnd.console.warn(e);
            error.@im.actor.runtime.js.entity.JsClosureError::onError(*)(e);
        });
    }-*/;

    public final native void setLocalDescription(JsSessionDescription description, JsClosure closure, JsClosureError error)/*-{
        this.peerConnection.setLocalDescription(description, function() {
            closure.@im.actor.runtime.js.entity.JsClosure::callback(*)();
        }, function(e) {
            $wnd.console.warn(e);
            error.@im.actor.runtime.js.entity.JsClosureError::onError(*)(e);
        });
    }-*/;

    public final native void setListener(JsPeerConnectionListener listener)/*-{
        this.peerConnection.onicecandidate = function(candidate) {
            if (candidate.candidate != null) {
                listener.@im.actor.runtime.js.webrtc.JsPeerConnectionListener::onIceCandidate(*)(candidate.candidate);
            }
        };
        this.peerConnection.onaddstream = function(event) {
            listener.@im.actor.runtime.js.webrtc.JsPeerConnectionListener::onStreamAdded(*)(event.stream);
        }
        this.peerConnection.onremovestream = function(event) {
            listener.@im.actor.runtime.js.webrtc.JsPeerConnectionListener::onStreamRemoved(*)(event.stream);
        }
        this.peerConnection.onnegotiationneeded = function(event) {
            listener.@im.actor.runtime.js.webrtc.JsPeerConnectionListener::onRenegotiationNeeded(*)();
        }
    }-*/;

}
