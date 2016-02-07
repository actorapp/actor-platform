package im.actor.core.js.providers;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import im.actor.core.Messenger;
import im.actor.core.js.JsMessenger;
import im.actor.core.js.modules.JsScheduller;
import im.actor.core.js.providers.webrtc.JsAudio;
import im.actor.core.js.providers.webrtc.JsIceServer;
import im.actor.core.js.providers.webrtc.JsPeerConnection;
import im.actor.core.js.providers.webrtc.JsPeerConnectionConfig;
import im.actor.core.js.providers.webrtc.JsPeerConnectionListener;
import im.actor.core.js.providers.webrtc.JsRTCIceCandidate;
import im.actor.core.js.providers.webrtc.JsSessionDescription;
import im.actor.core.js.providers.webrtc.JsStreaming;
import im.actor.core.js.providers.webrtc.JsMediaStream;
import im.actor.core.webrtc.WebRTCController;
import im.actor.core.webrtc.WebRTCProvider;
import im.actor.runtime.Log;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;
import im.actor.runtime.promise.Promise;

public class JsWebRTCProvider implements WebRTCProvider {

    private static final String TAG = "JsWebRTCProvider";

    private WebRTCController controller;
    private JsMessenger messenger;
    private JsPeerConnection peerConnection;
    private JsAudio voicePlayback;
    private JsMediaStream voiceCapture;

    private long runningCallId;

    @Override
    public void init(Messenger messenger, WebRTCController controller) {
        this.controller = controller;
        this.messenger = (JsMessenger) messenger;
    }

    @Override
    public void onIncomingCall(long callId) {
        runningCallId = callId;

        messenger.broadcastEvent("call", callEvent("" + callId, "incoming"));
    }

    @Override
    public void onOutgoingCall(long callId) {
        runningCallId = callId;

        messenger.broadcastEvent("call", callEvent("" + callId, "outgoing"));
    }

    private final native JavaScriptObject callEvent(String id, String type)/*-{
        return {id: id, type: type};
    }-*/;

    private void createPeerConnection(final long callId) {
        JsArray<JsIceServer> servers = JsArray.createArray().cast();
        servers.push(JsIceServer.create("stun:62.4.22.219:3478"));
        servers.push(JsIceServer.create("turn:62.4.22.219:3478?transport=tcp", "actor", "password"));
        servers.push(JsIceServer.create("turn:62.4.22.219:3478?transport=udp", "actor", "password"));
        peerConnection = JsPeerConnection.create(JsPeerConnectionConfig.create(servers));
        peerConnection.setListener(new JsPeerConnectionListener() {
            @Override
            public void onIceCandidate(JsRTCIceCandidate candidate) {
                if (runningCallId == callId) {
                    controller.sendCandidate(candidate.getLabel(), candidate.getId(), candidate.getSDP());
                }
            }

            @Override
            public void onIceCandidatesEnded() {

            }

            @Override
            public void onStreamAdded(JsMediaStream stream) {
                if (runningCallId == callId) {
                    Log.d(TAG, "onStreamAdded");
                    voicePlayback = JsAudio.create();
                    voicePlayback.setStream(stream);
                    voicePlayback.play();
                }
            }
        });
    }


    private <T> Promise<T> createAudioStream(final long callId, final T val) {
        return JsStreaming.getUserAudio().map(new Function<JsMediaStream, T>() {
            @Override
            public T apply(JsMediaStream mediaStream) {
                if (runningCallId == callId) {
                    Log.d(TAG, "Audio is created");
                    voiceCapture = mediaStream;
                    peerConnection.addStream(voiceCapture);
                    return val;
                } else {
                    mediaStream.stopAll();
                    throw new RuntimeException("Obsolete connection");
                }
            }
        });
    }

    @Override
    public void onOfferNeeded(final long callId) {
        Log.d(TAG, "onOfferNeeded");

        createPeerConnection(callId);

        createAudioStream(callId, "nothing").mapPromise(new Function<String, Promise<JsSessionDescription>>() {
            @Override
            public Promise<JsSessionDescription> apply(String mediaStream) {
                return peerConnection.createOffer();
            }
        }).mapPromise(new Function<JsSessionDescription, Promise<JsSessionDescription>>() {
            @Override
            public Promise<JsSessionDescription> apply(JsSessionDescription description) {
                if (runningCallId == callId) {
                    return peerConnection.setLocalDescription(description);
                } else {
                    throw new RuntimeException("Obsolete connection");
                }
            }
        }).then(new Consumer<JsSessionDescription>() {
            @Override
            public void apply(JsSessionDescription jsSessionDescription) {
                if (runningCallId == callId) {
                    controller.sendOffer(jsSessionDescription.getSDP());
                }
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                if (runningCallId == callId) {
                    Log.w(TAG, "Unable to create offer");
                    controller.endCall();
                }
            }
        }).done(JsScheduller.scheduller());
    }

    @Override
    public void onAnswerReceived(final long callId, String offerSDP) {
        Log.d(TAG, "onAnswerReceived");
        peerConnection.setRemoteDescription(JsSessionDescription.createAnswer(offerSDP)).then(new Consumer<JsSessionDescription>() {
            @Override
            public void apply(JsSessionDescription description) {
                if (runningCallId == callId) {
                    controller.readyForCandidates();
                }
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                if (runningCallId == callId) {
                    Log.w(TAG, "Unable to process answer");
                    controller.endCall();
                }
            }
        }).done(JsScheduller.scheduller());
    }

    @Override
    public void onOfferReceived(final long callId, final String offerSDP) {
        Log.d(TAG, "onOfferReceived");

        createPeerConnection(callId);

        createAudioStream(callId, "nothing").mapPromise(new Function<String, Promise<JsSessionDescription>>() {
            @Override
            public Promise<JsSessionDescription> apply(String mediaStream) {
                Log.d(TAG, "Set Remote");
                return peerConnection.setRemoteDescription(JsSessionDescription.createOffer(offerSDP));
            }
        }).mapPromise(new Function<JsSessionDescription, Promise<JsSessionDescription>>() {
            @Override
            public Promise<JsSessionDescription> apply(JsSessionDescription s) {
                Log.d(TAG, "Create answer");
                if (runningCallId == callId) {
                    return peerConnection.createAnswer();
                } else {
                    throw new RuntimeException("Obsolete connection");
                }
            }
        }).mapPromise(new Function<JsSessionDescription, Promise<JsSessionDescription>>() {
            @Override
            public Promise<JsSessionDescription> apply(JsSessionDescription description) {
                Log.d(TAG, "Set Local");
                if (runningCallId == callId) {
                    return peerConnection.setLocalDescription(description);
                } else {
                    throw new RuntimeException("Obsolete connection");
                }
            }
        }).then(new Consumer<JsSessionDescription>() {
            @Override
            public void apply(JsSessionDescription jsSessionDescription) {
                Log.w(TAG, "Sending answer1");
                if (runningCallId == callId) {
                    Log.w(TAG, "Sending answer");

                    controller.sendAnswer(jsSessionDescription.getSDP());
                    controller.readyForCandidates();
                }
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                Log.w(TAG, "Unable to create answer1");
                if (runningCallId == callId) {
                    Log.w(TAG, "Unable to create answer");
                    controller.endCall();
                }
            }
        }).done(JsScheduller.scheduller());
    }

    @Override
    public void onCandidate(long callId, String id, int label, String sdp) {
        Log.d(TAG, "onCandidate");
        peerConnection.addIceCandidate(label, sdp);
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
