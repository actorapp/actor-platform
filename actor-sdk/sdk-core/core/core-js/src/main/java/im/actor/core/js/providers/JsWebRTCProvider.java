package im.actor.core.js.providers;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;

import java.util.ArrayList;

import im.actor.core.WebRTCProvider;
import im.actor.core.entity.signals.AbsSignal;
import im.actor.core.entity.signals.AnswerSignal;
import im.actor.core.entity.signals.CandidateSignal;
import im.actor.core.entity.signals.OfferSignal;
import im.actor.core.js.JsMessenger;
import im.actor.core.js.providers.webrtc.JsIceCandidateEvent;
import im.actor.core.js.providers.webrtc.JsIceServer;
import im.actor.core.js.providers.webrtc.JsPeerConnection;
import im.actor.core.js.providers.webrtc.JsPeerConnectionConfig;
import im.actor.core.js.providers.webrtc.JsPeerConnectionListener;
import im.actor.core.js.providers.webrtc.JsSessionDescription;
import im.actor.core.js.providers.webrtc.JsStreaming;
import im.actor.core.js.providers.webrtc.JsUserMediaStream;
import im.actor.runtime.Log;
import im.actor.runtime.function.Consumer;

public class JsWebRTCProvider implements WebRTCProvider {

    private static final String TAG = "JsWebRTCProvider";

    private JsPeerConnection peerConnection;
    private JsUserMediaStream mediaStream;
    private boolean isReady = false;
    private ArrayList<AbsSignal> pendingSignals;

    @Override
    public void onIncomingCall() {
        Log.d(TAG, "onIncomingCall");
        pendingSignals = new ArrayList<>();
        JsMessenger.getInstance().callAnswer();
        JsArray<JsIceServer> servers = JsArray.createArray().cast();
        servers.push(JsIceServer.create("stun:62.4.22.219:3478"));
        servers.push(JsIceServer.create("turn:62.4.22.219:3478?transport=tcp", "actor", "password"));
        servers.push(JsIceServer.create("turn:62.4.22.219:3478?transport=udp", "actor", "password"));
        peerConnection = JsPeerConnection.create(JsPeerConnectionConfig.create(servers));
        peerConnection.setListener(new JsPeerConnectionListener() {
            @Override
            public void onIceCandidate(JsIceCandidateEvent candidate) {
                Log.d(TAG, "onIceCandidate: " + JsonUtils.stringify(candidate));
                JsMessenger.getInstance().callSendSignaling(new CandidateSignal(candidate.getId(),
                        candidate.getLabel(), candidate.getCandidate()));
            }
        });
        JsStreaming.getUserAudio().then(new Consumer<JsUserMediaStream>() {
            @Override
            public void apply(JsUserMediaStream jsUserMediaStream) {
                Log.d(TAG, "Audio is created");
                peerConnection.addStream(jsUserMediaStream);
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                Log.d(TAG, "Audio failured");
            }
        });
    }

    @Override
    public void onOutgoingCall() {
        Log.d(TAG, "onOutgoingCall");
    }

    @Override
    public void onCallStarted() {
        Log.d(TAG, "onCallStarted");
    }

    @Override
    public void onSignalingReceived(AbsSignal signal) {
        Log.d(TAG, "onSignalingReceived: " + signal);
        if (signal instanceof OfferSignal) {
            String sdp = ((OfferSignal) signal).getSdp();
            JsSessionDescription description = JsSessionDescription.createOffer(sdp);
            peerConnection.setRemoteDescription(description).then(new Consumer<String>() {
                @Override
                public void apply(String s) {
                    Log.d(TAG, "Description set");
                    peerConnection.createAnswer().then(new Consumer<JsSessionDescription>() {
                        @Override
                        public void apply(JsSessionDescription jsSessionDescription) {
                            peerConnection.setLocalDescription(jsSessionDescription);
                            Log.d(TAG, "Session desc: " + jsSessionDescription);
                            JsMessenger.getInstance().callSendSignaling(new AnswerSignal(jsSessionDescription.getSDP()));
                        }
                    }).failure(new Consumer<Exception>() {
                        @Override
                        public void apply(Exception e) {
                            Log.d(TAG, "Description desc error");
                        }
                    });
                }
            }).failure(new Consumer<Exception>() {
                @Override
                public void apply(Exception e) {
                    Log.d(TAG, "Description error");
                }
            });

        } else if (signal instanceof CandidateSignal) {
            CandidateSignal candidateSignal = (CandidateSignal) signal;
            try {
                peerConnection.addIceCandidate(candidateSignal.getLabel(), candidateSignal.getSdp());
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        }
    }

    @Override
    public void onCallEnded() {
        Log.d(TAG, "onCallEnded");

        // TODO: Close
        peerConnection = null;
        mediaStream = null;
    }
}
