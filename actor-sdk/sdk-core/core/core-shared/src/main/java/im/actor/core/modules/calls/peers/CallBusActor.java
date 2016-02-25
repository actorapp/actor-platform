package im.actor.core.modules.calls.peers;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import im.actor.core.api.ApiAdvertiseMaster;
import im.actor.core.api.ApiAdvertiseSelf;
import im.actor.core.api.ApiAnswer;
import im.actor.core.api.ApiCandidate;
import im.actor.core.api.ApiEnableConnection;
import im.actor.core.api.ApiNeedDisconnect;
import im.actor.core.api.ApiNeedOffer;
import im.actor.core.api.ApiOffer;
import im.actor.core.api.ApiWebRTCSignaling;
import im.actor.core.api.rpc.RequestJoinCall;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.eventbus.EventBusActor;
import im.actor.runtime.Log;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.webrtc.WebRTCMediaStream;

public class CallBusActor extends EventBusActor implements PeerCallCallback {

    private static final int STASH = 1;

    private final PeerSettings selfSettings;
    private final PeerCallCallback peerCallback;
    private final CallBusCallback callBusCallback;
    private boolean isMasterReady;
    private long masterDeviceId;
    private PeerCallInt peerCall;

    public CallBusActor(final CallBusCallback callBusCallback, PeerSettings selfSettings, ModuleContext context) {
        super(context);

        this.selfSettings = selfSettings;
        this.callBusCallback = callBusCallback;
        this.peerCallback = new CallbackWrapper(this);
    }


    @Override
    public void preStart() {
        super.preStart();

        ActorRef ref = system().actorOf(getPath() + "/peer", new ActorCreator() {
            @Override
            public Actor create() {
                return new PeerCallActor(peerCallback, CallBusActor.this.selfSettings, context());
            }
        });
        this.peerCall = new PeerCallInt(ref);
    }

    @Override
    public void onBusStarted() {
        super.onBusStarted();
        callBusCallback.onBusStarted(getBusId());
    }


    //
    // PeerCall callback
    //

    @Override
    public void onOffer(long deviceId, String sdp) {
        sendSignal(deviceId, new ApiOffer(0, sdp, CallBusActor.this.selfSettings.toApi()));
    }

    @Override
    public void onAnswer(long deviceId, String sdp) {
        sendSignal(deviceId, new ApiAnswer(0, sdp));
    }

    @Override
    public void onCandidate(long deviceId, int mdpIndex, String id, String sdp) {
        sendSignal(deviceId, new ApiCandidate(0, mdpIndex, id, sdp));
    }

    @Override
    public void onPeerStateChanged(long deviceId, PeerState state) {

    }

    @Override
    public void onStreamAdded(long deviceId, WebRTCMediaStream stream) {

    }

    @Override
    public void onStreamRemoved(long deviceId, WebRTCMediaStream stream) {

    }


    //
    // Actions
    //

    public void onAnswerCall() {
        // sendSignal(masterDeviceId, new ApiAnswerCall());
    }

    public void onRejectCall() {
        // sendSignal(masterDeviceId, new ApiRejectCall());
    }

    public void onChangeMute(boolean isMuted) {
        peerCall.onMuteChanged(isMuted);
    }

    //
    // Event Bus handler
    //

    @Override
    public void onDeviceConnected(int uid, long deviceId) {

    }

    @Override
    public void onDeviceDisconnected(int uid, long deviceId) {
        peerCall.disposePeer(deviceId);
    }

    @Override
    public final void onMessageReceived(@Nullable Integer senderId, @Nullable Long senderDeviceId, byte[] data) {
        if (senderId == null || senderDeviceId == null) {
            return;
        }

        ApiWebRTCSignaling signal;
        try {
            signal = ApiWebRTCSignaling.fromBytes(data);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Log.d("CallBusActor", "Message Received: " + signal);

        if (signal instanceof ApiAnswer) {
            ApiAnswer answer = (ApiAnswer) signal;
            peerCall.onAnswer(senderDeviceId, answer.getSdp());
        } else if (signal instanceof ApiOffer) {
            ApiOffer offer = (ApiOffer) signal;
            peerCall.onAdvertised(senderDeviceId, new PeerSettings(offer.getOwnPeerSettings()));
            peerCall.onOffer(senderDeviceId, offer.getSdp());
        } else if (signal instanceof ApiCandidate) {
            ApiCandidate candidate = (ApiCandidate) signal;
            peerCall.onCandidate(senderDeviceId, candidate.getIndex(), candidate.getId(), candidate.getSdp());
        } else if (signal instanceof ApiNeedOffer) {
            ApiNeedOffer needOffer = (ApiNeedOffer) signal;
            peerCall.onAdvertised(needOffer.getDevice(), new PeerSettings(needOffer.getPeerSettings()));
            peerCall.onOfferNeeded(needOffer.getDevice());
        } else if (signal instanceof ApiNeedDisconnect) {
            ApiNeedDisconnect disconnect = (ApiNeedDisconnect) signal;
            peerCall.disposePeer(disconnect.getDevice());
        } else if (signal instanceof ApiEnableConnection) {
            ApiEnableConnection connection = (ApiEnableConnection) signal;
            peerCall.onOwnStarted();
            peerCall.onTheirStarted(connection.getDevice());
        } else if (signal instanceof ApiAdvertiseMaster) {
            if (isMasterReady) {
                return;
            }
            isMasterReady = true;
            masterDeviceId = senderDeviceId;
            unstashAll(STASH);

            //
            // Advertise own settings to master device
            //
            sendSignal(masterDeviceId, new ApiAdvertiseSelf(selfSettings.toApi()));

            //
            // Automatically start master device
            //
            peerCall.onTheirStarted(masterDeviceId);
        }
    }

    public final void sendSignal(long deviceId, ApiWebRTCSignaling signal) {
        Log.d("CallBusActor", "Message Sent: " + signal);
        try {
            sendMessage(deviceId, signal.buildContainer());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof JoinBus) {
            joinBus(((JoinBus) message).getBusId());
        } else if (message instanceof CreateBus) {
            createBus();
        } else if (message instanceof SendSignal) {
            SendSignal signal = (SendSignal) message;
            sendSignal(signal.getDeviceId(), signal.getSignal());
        } else if (message instanceof AnswerCall) {
            if (!isMasterReady) {
                stash(STASH);
                return;
            }
            onAnswerCall();
        } else if (message instanceof RejectCall) {
            if (!isMasterReady) {
                stash(STASH);
                return;
            }
            onRejectCall();
        } else if (message instanceof Mute) {
            onChangeMute(((Mute) message).isMuted());
        } else {
            super.onReceive(message);
        }
    }

    public static class JoinBus {

        private String busId;

        public JoinBus(String busId) {
            this.busId = busId;
        }

        public String getBusId() {
            return busId;
        }
    }

    public static class CreateBus {

    }

    public static class SendSignal {

        private long deviceId;
        private ApiWebRTCSignaling signal;

        public SendSignal(long deviceId, ApiWebRTCSignaling signal) {
            this.deviceId = deviceId;
            this.signal = signal;
        }

        public long getDeviceId() {
            return deviceId;
        }

        public ApiWebRTCSignaling getSignal() {
            return signal;
        }
    }

    public static class AnswerCall {

    }

    public static class RejectCall {

    }

    public static class Mute {
        private boolean isMuted;

        public Mute(boolean isMuted) {
            this.isMuted = isMuted;
        }

        public boolean isMuted() {
            return isMuted;
        }
    }

    public class CallbackWrapper implements PeerCallCallback {

        private final PeerCallCallback callCallback;

        public CallbackWrapper(PeerCallCallback callCallback) {
            this.callCallback = callCallback;
        }

        @Override
        public void onOffer(final long deviceId, final String sdp) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    callCallback.onOffer(deviceId, sdp);
                }
            });
        }

        @Override
        public void onAnswer(final long deviceId, final String sdp) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    callCallback.onAnswer(deviceId, sdp);
                }
            });
        }

        @Override
        public void onCandidate(final long deviceId, final int mdpIndex, final String id, final String sdp) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    callCallback.onCandidate(deviceId, mdpIndex, id, sdp);
                }
            });
        }

        @Override
        public void onPeerStateChanged(final long deviceId, final PeerState state) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    callCallback.onPeerStateChanged(deviceId, state);
                }
            });
        }

        @Override
        public void onStreamAdded(final long deviceId, final WebRTCMediaStream stream) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    callCallback.onStreamAdded(deviceId, stream);
                }
            });
        }

        @Override
        public void onStreamRemoved(final long deviceId, final WebRTCMediaStream stream) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    callCallback.onStreamRemoved(deviceId, stream);
                }
            });
        }
    }
}
