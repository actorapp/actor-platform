package im.actor.core.js.providers;

import com.google.gwt.core.client.JsArray;

import java.util.ArrayList;

import im.actor.core.entity.Peer;
import im.actor.core.entity.signals.AbsSignal;
import im.actor.core.entity.signals.AnswerSignal;
import im.actor.core.entity.signals.CandidateSignal;
import im.actor.core.entity.signals.OfferSignal;
import im.actor.core.js.providers.webrtc.JsAudio;
import im.actor.core.js.providers.webrtc.JsIceServer;
import im.actor.core.js.providers.webrtc.JsPeerConnection;
import im.actor.core.js.providers.webrtc.JsPeerConnectionConfig;
import im.actor.core.js.providers.webrtc.JsPeerConnectionListener;
import im.actor.core.js.providers.webrtc.JsRTCIceCandidate;
import im.actor.core.js.providers.webrtc.JsSessionDescription;
import im.actor.core.js.providers.webrtc.JsStreaming;
import im.actor.core.js.providers.webrtc.JsMediaStream;
import im.actor.core.viewmodel.UserVM;
import im.actor.core.webrtc.WebRTCController;
import im.actor.core.webrtc.WebRTCProvider;
import im.actor.runtime.Log;
import im.actor.runtime.function.Consumer;

public class JsWebRTCProvider implements WebRTCProvider {

    private static final String TAG = "JsWebRTCProvider";

    private WebRTCController controller;
    private JsPeerConnection peerConnection;
    private JsAudio voicePlayback;
    private JsMediaStream voiceCapture;

    private boolean isReady = false;
    private ArrayList<CandidateSignal> pendingCandidates;
    private long runningCallId;

    @Override
    public void init(WebRTCController controller) {
        this.controller = controller;
    }

    @Override
    public void onIncomingCall(final long callId, Peer peer, UserVM[] users) {

        runningCallId = callId;

        controller.answerCall();

        Log.d(TAG, "onIncomingCall");
        pendingCandidates = new ArrayList<>();
        isReady = false;
        JsArray<JsIceServer> servers = JsArray.createArray().cast();
        servers.push(JsIceServer.create("stun:62.4.22.219:3478"));
        servers.push(JsIceServer.create("turn:62.4.22.219:3478?transport=tcp", "actor", "password"));
        servers.push(JsIceServer.create("turn:62.4.22.219:3478?transport=udp", "actor", "password"));
        peerConnection = JsPeerConnection.create(JsPeerConnectionConfig.create(servers));
        peerConnection.setListener(new JsPeerConnectionListener() {
            @Override
            public void onIceCandidate(JsRTCIceCandidate candidate) {
                if (runningCallId != callId) {
                    return;
                }

                // Log.d(TAG, "onIceCandidate: " + JsonUtils.stringify(candidate));

                controller.sendSignaling(new CandidateSignal(
                        candidate.getId(),
                        candidate.getLabel(),
                        candidate.getSDP()));
            }

            @Override
            public void onIceCandidatesEnded() {
                if (runningCallId != callId) {
                    return;
                }

                // Log.d(TAG, "onIceCandidate Ended");
            }

            @Override
            public void onStreamAdded(JsMediaStream stream) {
                if (runningCallId != callId) {
                    return;
                }

                // Log.d(TAG, "onStreamAdded: " + JsonUtils.stringify(stream));

                voicePlayback = JsAudio.create();
                voicePlayback.setStream(stream);
                voicePlayback.play();
            }
        });

        JsStreaming.getUserAudio().then(new Consumer<JsMediaStream>() {
            @Override
            public void apply(JsMediaStream jsMediaStream) {
                if (runningCallId != callId) {
                    jsMediaStream.stopAll();
                    return;
                }

                Log.d(TAG, "Audio is created");
                voiceCapture = jsMediaStream;
                peerConnection.addStream(voiceCapture);
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                if (runningCallId != callId) {
                    return;
                }

                Log.d(TAG, "Audio failured");
            }
        });
    }

    @Override
    public void onOutgoingCall(long callId, Peer peer, UserVM[] users) {

    }

    @Override
    public void onCallStart(long callId) {

    }

    @Override
    public void onCallSignaling(final long callId, AbsSignal signal) {
        Log.d(TAG, "onSignalingReceived: " + signal);
        if (signal instanceof OfferSignal) {
            String sdp = ((OfferSignal) signal).getSdp();
            JsSessionDescription description = JsSessionDescription.createOffer(sdp);
            peerConnection.setRemoteDescription(description).then(new Consumer<String>() {
                @Override
                public void apply(String s) {
                    Log.d(TAG, "Description set");
                    if (callId != runningCallId) {
                        return;
                    }
                    peerConnection.createAnswer().then(new Consumer<JsSessionDescription>() {
                        @Override
                        public void apply(JsSessionDescription jsSessionDescription) {
                            if (callId != runningCallId) {
                                return;
                            }

                            peerConnection.setLocalDescription(jsSessionDescription);
                            Log.d(TAG, "Session desc: " + jsSessionDescription);
                            controller.sendSignaling(new AnswerSignal(jsSessionDescription.getSDP()));

                            isReady = true;
                            for (CandidateSignal signal1 : pendingCandidates) {
                                applySignals(signal1);
                            }
                            pendingCandidates.clear();
                        }
                    }).failure(new Consumer<Exception>() {
                        @Override
                        public void apply(Exception e) {
                            if (callId != runningCallId) {
                                return;
                            }
                            Log.d(TAG, "Description desc error");
                        }
                    });
                }
            }).failure(new Consumer<Exception>() {
                @Override
                public void apply(Exception e) {
                    if (callId != runningCallId) {
                        return;
                    }
                    Log.d(TAG, "Description error");
                }
            });

        } else if (signal instanceof CandidateSignal) {
            CandidateSignal candidateSignal = (CandidateSignal) signal;
            if (!isReady) {
                pendingCandidates.add(candidateSignal);
            } else {
                applySignals(candidateSignal);
            }
        }
    }

    private void applySignals(CandidateSignal signal) {
        try {
            Log.d(TAG, "applySignals: " + signal);
            peerConnection.addIceCandidate(signal.getLabel(), signal.getSdp());
        } catch (Exception e) {
            Log.e(TAG, e);
        }
    }

    @Override
    public void onCallEnd(long callId) {
        if (voicePlayback != null) {
            voicePlayback.reset();
            voicePlayback = null;
        }
        if (voiceCapture != null) {
            voiceCapture.stopAll();
            voiceCapture = null;
        }
        if (peerConnection != null) {
            peerConnection.close();
            peerConnection = null;
        }
    }
}
