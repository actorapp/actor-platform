package im.actor.runtime.android.webrtc;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;

import org.jetbrains.annotations.NotNull;


import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

import im.actor.runtime.android.AndroidContext;
import im.actor.runtime.android.AndroidWebRTCRuntimeProvider;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.PromiseResolver;
import im.actor.runtime.webrtc.WebRTCIceServer;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;
import im.actor.runtime.webrtc.WebRTCPeerConnectionCallback;
import im.actor.runtime.webrtc.WebRTCSessionDescription;
import im.actor.runtime.webrtc.WebRTCSettings;

public class AndroidPeerConnection implements WebRTCPeerConnection {

    private static final boolean LIBJINGLE_LOGS = false;

    public AndroidPeerConnection(WebRTCIceServer[] webRTCIceServers, WebRTCSettings settings) {

        if (LIBJINGLE_LOGS) {
            Logging.enableTracing("logcat:",
                    EnumSet.of(Logging.TraceLevel.TRACE_ALL),
                    Logging.Severity.LS_SENSITIVE);

        }

        final ArrayList<PeerConnection.IceServer> servers = new ArrayList<>();
        PeerConnection.IceServer ice;
        for (WebRTCIceServer webRTCIceServer : webRTCIceServers) {
            if (webRTCIceServer.getUsername() != null) {
                ice = new PeerConnection.IceServer(
                        webRTCIceServer.getUrl(),
                        webRTCIceServer.getUsername(),
                        webRTCIceServer.getCredential() == null ? "" : webRTCIceServer.getCredential());
            } else {
                ice = new PeerConnection.IceServer(webRTCIceServer.getUrl());
            }
            servers.add(ice);

        }
        AndroidWebRTCRuntimeProvider.postToHandler(new Runnable() {
            @Override
            public void run() {
                AndroidPeerConnection.this.peerConnection = AndroidWebRTCRuntimeProvider.FACTORY.createPeerConnection(servers, new MediaConstraints(), new PeerConnection.Observer() {
                    @Override
                    public void onSignalingChange(PeerConnection.SignalingState signalingState) {

                    }

                    @Override
                    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

                    }

                    @Override
                    public void onIceConnectionReceivingChange(boolean b) {

                    }

                    @Override
                    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

                    }

                    @Override
                    public void onIceCandidate(IceCandidate candidate) {
                        for (WebRTCPeerConnectionCallback c : callbacks) {
                            c.onCandidate(candidate.sdpMLineIndex, candidate.sdpMid, candidate.sdp);
                        }
                    }

                    @Override
                    public void onAddStream(MediaStream stream) {
                        AndroidMediaStream stream1 = new AndroidMediaStream(stream);
                        streams.put(stream, stream1);
                        for (WebRTCPeerConnectionCallback c : callbacks) {
                            c.onStreamAdded(stream1);
                        }
                    }

                    @Override
                    public void onRemoveStream(MediaStream stream) {
                        AndroidMediaStream stream1 = streams.get(stream);
                        if (stream1 != null) {
                            for (WebRTCPeerConnectionCallback c : callbacks) {
                                c.onStreamRemoved(stream1);
                            }
                        }
                    }

                    @Override
                    public void onDataChannel(DataChannel dataChannel) {

                    }

                    @Override
                    public void onRenegotiationNeeded() {
                        for (WebRTCPeerConnectionCallback c : callbacks) {
                            c.onRenegotiationNeeded();
                        }
                    }
                });
            }
        });
    }

    private PeerConnection peerConnection;
    private ArrayList<WebRTCPeerConnectionCallback> callbacks = new ArrayList<>();
    private HashMap<MediaStream, AndroidMediaStream> streams = new HashMap<>();


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
    public void addCandidate(final int index, @NotNull final String id, @NotNull final String sdp) {
        AndroidWebRTCRuntimeProvider.postToHandler(new Runnable() {
            @Override
            public void run() {
                peerConnection.addIceCandidate(new IceCandidate(id, index, sdp));
            }
        });
    }

    @Override
    public void addOwnStream(@NotNull final WebRTCMediaStream stream) {
        AndroidWebRTCRuntimeProvider.postToHandler(new Runnable() {
            @Override
            public void run() {
                peerConnection.addStream(((AndroidMediaStream) stream).getStream());
            }
        });
    }

    @Override
    public void removeOwnStream(@NotNull final WebRTCMediaStream stream) {
        AndroidWebRTCRuntimeProvider.postToHandler(new Runnable() {
            @Override
            public void run() {
                peerConnection.removeStream(((AndroidMediaStream) stream).getStream());
            }
        });
    }

    @NotNull
    @Override
    public Promise<WebRTCSessionDescription> setLocalDescription(@NotNull final WebRTCSessionDescription description) {
        return new Promise<>(new PromiseFunc<WebRTCSessionDescription>() {
            @Override
            public void exec(@NonNull @NotNull final PromiseResolver<WebRTCSessionDescription> resolver) {
                AndroidWebRTCRuntimeProvider.postToHandler(new Runnable() {
                    @Override
                    public void run() {
                        peerConnection.setLocalDescription(new SdpObserver() {
                            @Override
                            public void onCreateSuccess(SessionDescription sessionDescription) {
                                //we are just setting here
                            }

                            @Override
                            public void onSetSuccess() {
                                resolver.result(description);
                            }

                            @Override
                            public void onCreateFailure(String s) {
                                //we are just setting here
                            }

                            @Override
                            public void onSetFailure(String s) {
                                resolver.error(new Exception("setLocalDescription:onSetFailure"));

                            }
                        }, new SessionDescription(description.getType().equals("offer") ? SessionDescription.Type.OFFER : SessionDescription.Type.ANSWER, description.getSdp()));
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
            public void exec(@NonNull @NotNull final PromiseResolver<WebRTCSessionDescription> resolver) {
                AndroidWebRTCRuntimeProvider.postToHandler(new Runnable() {
                    @Override
                    public void run() {
                        peerConnection.setRemoteDescription(new SdpObserver() {
                            @Override
                            public void onCreateSuccess(SessionDescription sessionDescription) {
                                //we are just setting here
                            }

                            @Override
                            public void onSetSuccess() {
                                resolver.result(description);
                            }

                            @Override
                            public void onCreateFailure(String s) {
                                //we are just setting here
                            }

                            @Override
                            public void onSetFailure(String s) {
                                resolver.error(new Exception("setRemoteDescription:onSetFailure"));

                            }
                        }, new SessionDescription(description.getType().equals("offer") ? SessionDescription.Type.OFFER : SessionDescription.Type.ANSWER, description.getSdp()));
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
            public void exec(@NonNull @NotNull final PromiseResolver<WebRTCSessionDescription> resolver) {
                AndroidWebRTCRuntimeProvider.postToHandler(new Runnable() {
                    @Override
                    public void run() {
                        peerConnection.createOffer(new SdpObserver() {
                            @Override
                            public void onCreateSuccess(SessionDescription sessionDescription) {
                                resolver.result(new WebRTCSessionDescription(sessionDescription.type.name().toLowerCase(), sessionDescription.description));
                            }

                            @Override
                            public void onSetSuccess() {
                                //we are just creating here
                            }

                            @Override
                            public void onCreateFailure(String s) {
                                resolver.error(new Exception("createOffer:onCreateFailure"));
                            }

                            @Override
                            public void onSetFailure(String s) {
                                //we are just creating here
                            }
                        }, new MediaConstraints());
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
            public void exec(@NonNull @NotNull final PromiseResolver<WebRTCSessionDescription> resolver) {
                AndroidWebRTCRuntimeProvider.postToHandler(new Runnable() {
                    @Override
                    public void run() {
                        peerConnection.createAnswer(new SdpObserver() {
                            @Override
                            public void onCreateSuccess(SessionDescription sessionDescription) {
                                resolver.result(new WebRTCSessionDescription(sessionDescription.type.name().toLowerCase(), sessionDescription.description));
                            }

                            @Override
                            public void onSetSuccess() {
                                //we are just creating here
                            }

                            @Override
                            public void onCreateFailure(String s) {
                                resolver.error(new Exception("createAnswer:onCreateFailure"));
                            }

                            @Override
                            public void onSetFailure(String s) {
                                //we are just creating here
                            }
                        }, new MediaConstraints());
                    }
                });

            }
        });
    }

    @Override
    public void close() {
        AndroidWebRTCRuntimeProvider.postToHandler(new Runnable() {
            @Override
            public void run() {
                peerConnection.dispose();
            }
        });


    }
}
