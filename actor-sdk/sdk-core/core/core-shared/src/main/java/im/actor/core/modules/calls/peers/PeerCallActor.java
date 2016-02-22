package im.actor.core.modules.calls.peers;

import java.util.HashMap;

import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.entity.PeerNodeSettings;
import im.actor.core.modules.calls.peers.messages.RTCAdvertised;
import im.actor.core.modules.calls.peers.messages.RTCAnswer;
import im.actor.core.modules.calls.peers.messages.RTCCandidate;
import im.actor.core.modules.calls.peers.messages.RTCDispose;
import im.actor.core.modules.calls.peers.messages.RTCNeedOffer;
import im.actor.core.modules.calls.peers.messages.RTCOffer;
import im.actor.core.modules.calls.peers.messages.RTCOwnStart;
import im.actor.core.modules.calls.peers.messages.RTCStart;
import im.actor.core.util.ModuleActor;
import im.actor.runtime.Log;
import im.actor.runtime.WebRTC;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.webrtc.WebRTCMediaStream;

public class PeerCallActor extends ModuleActor implements PeerNodeCallback {

    private static final String TAG = "PeerCallActor";

    // Parent Actor for handling events
    private final PeerCallCallback callback;

    // Peer Settings
    private final PeerNodeSettings selfSettings;

    // WebRTC objects
    private HashMap<Long, PeerNodeInt> refs = new HashMap<>();
    private WebRTCMediaStream webRTCMediaStream;

    // State objects
    private boolean isOwnStarted = false;
    private boolean isOutputEnabled = false;

    public PeerCallActor(PeerCallCallback callback, PeerNodeSettings selfSettings, ModuleContext context) {
        super(context);
        this.callback = callback;
        this.selfSettings = selfSettings;
    }

    @Override
    public void preStart() {
        super.preStart();

        WebRTC.getUserAudio().then(new Consumer<WebRTCMediaStream>() {
            @Override
            public void apply(WebRTCMediaStream webRTCMediaStream) {
                PeerCallActor.this.webRTCMediaStream = webRTCMediaStream;
                // PeerCallActor.this.webRTCMediaStream.setEnabled(!isOutputEnabled);
                for (PeerNodeInt node : refs.values()) {
                    node.setOwnStream(webRTCMediaStream);
                }
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                Log.d(TAG, "Unable to load audio");
                self().send(PoisonPill.INSTANCE);
            }
        }).done(self());
    }


    //
    // Peer Node Callbacks
    //

    @Override
    public void onOffer(long deviceId, String sdp) {
        callback.onOffer(deviceId, sdp);
    }

    @Override
    public void onAnswer(long deviceId, String sdp) {
        callback.onAnswer(deviceId, sdp);
    }

    @Override
    public void onCandidate(long deviceId, int mdpIndex, String id, String sdp) {
        callback.onCandidate(deviceId, mdpIndex, id, sdp);
    }

    @Override
    public void onConnectionStarted(long deviceId) {
        callback.onConnectionStarted(deviceId);
    }

    @Override
    public void onConnectionEstablished(long deviceId) {
        callback.onConnectionEstablished(deviceId);
    }

    @Override
    public void onStreamAdded(long deviceId, WebRTCMediaStream stream) {
        callback.onStreamAdded(deviceId, stream);
    }

    @Override
    public void onStreamRemoved(long deviceId, WebRTCMediaStream stream) {
        callback.onStreamRemoved(deviceId, stream);
    }


    //
    // Media Settings
    //

    public void onMediaOutputChanged(boolean isEnabled) {
        this.isOutputEnabled = isEnabled;
        if (webRTCMediaStream != null) {
            // webRTCMediaStream.setEnabled(!isOutputEnabled);
        }
    }

    public void onOwnStart() {
        if (!isOwnStarted) {
            isOwnStarted = true;
            for (PeerNodeInt d : refs.values()) {
                d.startOwn();
            }
        }
    }

    //
    // Peer Collection
    //

    public PeerNodeInt getPeer(long deviceId) {
        if (!refs.containsKey(deviceId)) {
            return createNewPeer(deviceId);
        }
        return refs.get(deviceId);
    }

    public PeerNodeInt createNewPeer(final long deviceId) {
        ActorRef ref = system().actorOf(getPath() + "/" + deviceId, new ActorCreator() {
            @Override
            public Actor create() {
                return new PeerNodeActor(deviceId, selfSettings, new WrappedCallback(), context());
            }
        });
        PeerNodeInt peerNodeInt = new PeerNodeInt(deviceId, ref);
        if (webRTCMediaStream != null) {
            peerNodeInt.setOwnStream(webRTCMediaStream);
        }
        if (isOwnStarted) {
            peerNodeInt.startOwn();
        }
        refs.put(deviceId, peerNodeInt);
        return peerNodeInt;
    }

    public void disposePeer(long deviceId) {
        PeerNodeInt peerNodeInt = refs.remove(deviceId);
        if (peerNodeInt != null) {
            peerNodeInt.kill();
        }
    }


    @Override
    public void postStop() {
        super.postStop();
        for (PeerNodeInt d : refs.values()) {
            d.kill();
        }
        refs.clear();
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof RTCOwnStart) {
            onOwnStart();
        } else if (message instanceof RTCStart) {
            RTCStart start = (RTCStart) message;
            getPeer(start.getDeviceId()).startTheir();
        } else if (message instanceof RTCDispose) {
            RTCDispose dispose = (RTCDispose) message;
            disposePeer(dispose.getDeviceId());
        } else if (message instanceof RTCOffer) {
            RTCOffer offer = (RTCOffer) message;
            getPeer(offer.getDeviceId()).onOffer(offer.getSdp());
        } else if (message instanceof RTCAnswer) {
            RTCAnswer answer = (RTCAnswer) message;
            getPeer(answer.getDeviceId()).onAnswer(answer.getSdp());
        } else if (message instanceof RTCCandidate) {
            RTCCandidate candidate = (RTCCandidate) message;
            getPeer(candidate.getDeviceId()).onCandidate(candidate.getMdpIndex(),
                    candidate.getId(), candidate.getSdp());
        } else if (message instanceof RTCNeedOffer) {
            RTCNeedOffer needOffer = (RTCNeedOffer) message;
            getPeer(needOffer.getDeviceId()).onOfferNeeded();
        } else if (message instanceof RTCAdvertised) {
            RTCAdvertised advertised = (RTCAdvertised) message;
            getPeer(advertised.getDeviceId()).onAdvertised(advertised.getSettings());
        } else {
            super.onReceive(message);
        }
    }

    private class WrappedCallback implements PeerNodeCallback {

        @Override
        public void onOffer(final long deviceId, final String sdp) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    PeerCallActor.this.onOffer(deviceId, sdp);
                }
            });
        }

        @Override
        public void onAnswer(final long deviceId, final String sdp) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    PeerCallActor.this.onAnswer(deviceId, sdp);
                }
            });
        }

        @Override
        public void onCandidate(final long deviceId, final int mdpIndex, final String id, final String sdp) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    PeerCallActor.this.onCandidate(deviceId, mdpIndex, id, sdp);
                }
            });
        }

        @Override
        public void onConnectionStarted(final long deviceId) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    PeerCallActor.this.onConnectionStarted(deviceId);
                }
            });
        }

        @Override
        public void onConnectionEstablished(final long deviceId) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    PeerCallActor.this.onConnectionEstablished(deviceId);
                }
            });
        }

        @Override
        public void onStreamAdded(final long deviceId, final WebRTCMediaStream stream) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    PeerCallActor.this.onStreamAdded(deviceId, stream);
                }
            });
        }

        @Override
        public void onStreamRemoved(final long deviceId, final WebRTCMediaStream stream) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    PeerCallActor.this.onStreamRemoved(deviceId, stream);
                }
            });
        }
    }
}