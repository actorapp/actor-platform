package im.actor.runtime.js.webrtc;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import im.actor.runtime.js.entity.JsClosure;
import im.actor.runtime.js.entity.JsClosureError;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.PromiseResolver;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;
import im.actor.runtime.webrtc.WebRTCPeerConnectionCallback;
import im.actor.runtime.webrtc.WebRTCSessionDescription;

public class PeerConnection implements WebRTCPeerConnection {

    private JsPeerConnection peerConnection;
    private ArrayList<WebRTCPeerConnectionCallback> callbacks = new ArrayList<>();

    public PeerConnection(JsPeerConnection peerConnection) {
        this.peerConnection = peerConnection;
        this.peerConnection.setListener(new JsPeerConnectionListener() {
            @Override
            public void onIceCandidate(JsRTCIceCandidate candidate) {
                for (WebRTCPeerConnectionCallback c : callbacks) {
                    c.onCandidate(candidate.getLabel(), candidate.getId(), candidate.getSDP());
                }
            }

            @Override
            public void onIceCandidatesEnded() {

            }

            @Override
            public void onStreamAdded(JsMediaStream stream) {
                JsAudio audio = JsAudio.create();
                audio.setStream(stream);
                audio.play();
            }
        });
    }

    @Override
    public void addCallback(@NotNull WebRTCPeerConnectionCallback callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }

    @Override
    public void removeCallback(@NotNull WebRTCPeerConnectionCallback callback) {
        callbacks.remove(callback);
    }

    @Override
    public void addCandidate(int label, @NotNull String id, @NotNull String candidate) {
        peerConnection.addIceCandidate(label, candidate);
    }

    @Override
    public void addOwnStream(@NotNull WebRTCMediaStream stream) {
        if (stream instanceof JsMediaStream) {
            peerConnection.addStream((JsMediaStream) stream);
        }
    }

    @NotNull
    @Override
    public Promise<WebRTCSessionDescription> setLocalDescription(@NotNull final WebRTCSessionDescription description) {
        return new Promise<>(new PromiseFunc<WebRTCSessionDescription>() {
            @Override
            public void exec(@NotNull final PromiseResolver<WebRTCSessionDescription> resolver) {
                peerConnection.setLocalDescription(JsSessionDescription.create(description.getType(),
                        description.getSdp()), new JsClosure() {
                    @Override
                    public void callback() {
                        resolver.result(description);
                    }
                }, new JsClosureError() {
                    @Override
                    public void onError(JavaScriptObject error) {
                        resolver.error(new JavaScriptException(error));
                    }
                });
            }
        });
    }

    @NotNull
    @Override
    public Promise<WebRTCSessionDescription> setRemoteDescription(@NotNull final WebRTCSessionDescription description) {
        return new Promise<>(new PromiseFunc<WebRTCSessionDescription>() {
            @Override
            public void exec(@NotNull final PromiseResolver<WebRTCSessionDescription> resolver) {
                peerConnection.setRemoteDescription(JsSessionDescription.create(description.getType(),
                        description.getSdp()), new JsClosure() {
                    @Override
                    public void callback() {
                        resolver.result(description);
                    }
                }, new JsClosureError() {
                    @Override
                    public void onError(JavaScriptObject error) {
                        resolver.error(new JavaScriptException(error));
                    }
                });
            }
        });
    }

    @NotNull
    @Override
    public Promise<WebRTCSessionDescription> createOffer() {
        return new Promise<>(new PromiseFunc<WebRTCSessionDescription>() {
            @Override
            public void exec(@NotNull final PromiseResolver<WebRTCSessionDescription> resolver) {
                peerConnection.createOffer(new JsSessionDescriptionCallback() {
                    @Override
                    public void onOfferCreated(JsSessionDescription offer) {
                        resolver.result(new WebRTCSessionDescription(offer.getType(), offer.getSDP()));
                    }

                    @Override
                    public void onOfferFailure() {
                        resolver.error(new RuntimeException());
                    }
                });
            }
        });
    }

    @NotNull
    @Override
    public Promise<WebRTCSessionDescription> createAnswer() {
        return new Promise<>(new PromiseFunc<WebRTCSessionDescription>() {
            @Override
            public void exec(@NotNull final PromiseResolver<WebRTCSessionDescription> resolver) {
                peerConnection.createAnswer(new JsSessionDescriptionCallback() {
                    @Override
                    public void onOfferCreated(JsSessionDescription offer) {
                        resolver.result(new WebRTCSessionDescription(offer.getType(), offer.getSDP()));
                    }

                    @Override
                    public void onOfferFailure() {
                        resolver.error(new RuntimeException());
                    }
                });
            }
        });
    }

    @Override
    public void close() {
        peerConnection.close();
    }
}
