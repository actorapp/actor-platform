package im.actor.runtime.webrtc;

import org.jetbrains.annotations.NotNull;


import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.util.ArrayList;
import java.util.HashMap;

import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.PromiseResolver;

import static im.actor.runtime.WebRTCRuntimeProvider.factory;

public class AndroidPeerConnection implements WebRTCPeerConnection{
    PeerConnection pc;
    static ArrayList<PeerConnection.IceServer> iceServers;

    public AndroidPeerConnection() {
        if(iceServers == null){
            iceServers = new ArrayList<PeerConnection.IceServer>();
            iceServers.add(new PeerConnection.IceServer("stun:62.4.22.219:3478"));
            iceServers.add(new PeerConnection.IceServer("turn:62.4.22.219:3478?transport=tcp", "actor", "password"));
            iceServers.add(new PeerConnection.IceServer("turn:62.4.22.219:3478?transport=udp", "actor", "password"));

        }

        this.pc = factory().createPeerConnection(iceServers, new MediaConstraints(), new PeerConnection.Observer() {
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
    public void addCandidate(int index, @NotNull String id, @NotNull String sdp) {
        peerConnection.addIceCandidate(new IceCandidate(id, index, sdp));
    }

    @Override
    public void addOwnStream(@NotNull WebRTCMediaStream stream) {
        peerConnection.addStream(((AndroidMediaStream)stream).getStream());
    }

    @NotNull
    @Override
    public Promise<WebRTCSessionDescription> setLocalDescription(@NotNull final WebRTCSessionDescription description) {
        return new Promise<>(new PromiseFunc<WebRTCSessionDescription>() {
            @Override
            public void exec(@NotNull final PromiseResolver<WebRTCSessionDescription> resolver) {
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
                        resolver.error(new Exception("setLocalDescription:onSetFailure"));

                    }
                }, new SessionDescription(description.getType().equals("offer")? SessionDescription.Type.OFFER: SessionDescription.Type.ANSWER, description.getSdp()));
            }
        });
    }

    @NotNull
    @Override
    public Promise<WebRTCSessionDescription> setRemoteDescription(@NotNull final WebRTCSessionDescription description) {
        return new Promise<>(new PromiseFunc<WebRTCSessionDescription>() {
            @Override
            public void exec(@NotNull final PromiseResolver<WebRTCSessionDescription> resolver) {
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
                }, new SessionDescription(description.getType().equals("offer")? SessionDescription.Type.OFFER: SessionDescription.Type.ANSWER, description.getSdp()));
            }
        });
    }

    @NotNull
    @Override
    public Promise<WebRTCSessionDescription> createOffer() {
        return new Promise<>(new PromiseFunc<WebRTCSessionDescription>() {
            @Override
            public void exec(@NotNull final PromiseResolver<WebRTCSessionDescription> resolver) {
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

    @NotNull
    @Override
    public Promise<WebRTCSessionDescription> createAnswer() {
        return new Promise<>(new PromiseFunc<WebRTCSessionDescription>() {
            @Override
            public void exec(@NotNull final PromiseResolver<WebRTCSessionDescription> resolver) {
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

    @Override
    public void close() {
        for (AndroidMediaStream m:streams.values()) {
            peerConnection.removeStream(m.getStream());
            m.close();
        }
        peerConnection.close();
        peerConnection.dispose();

    }
}
