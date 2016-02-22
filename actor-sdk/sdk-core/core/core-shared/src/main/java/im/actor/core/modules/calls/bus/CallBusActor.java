package im.actor.core.modules.calls.bus;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;

import im.actor.core.api.ApiAnswer;
import im.actor.core.api.ApiCandidate;
import im.actor.core.api.ApiMembersChanged;
import im.actor.core.api.ApiNeedOffer;
import im.actor.core.api.ApiOffer;
import im.actor.core.api.ApiOnAnswer;
import im.actor.core.api.ApiSwitchMaster;
import im.actor.core.api.ApiWebRTCSignaling;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.peers.PeerCallActor;
import im.actor.core.modules.calls.peers.PeerCallCallback;
import im.actor.core.modules.calls.peers.PeerCallInt;
import im.actor.core.modules.calls.peers.PeerSettings;
import im.actor.core.modules.eventbus.EventBusActor;
import im.actor.runtime.Log;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.webrtc.WebRTCMediaStream;

public class CallBusActor extends EventBusActor {

    private HashMap<Long, Integer> deviceIds = new HashMap<>();
    private final PeerSettings selfSettings;
    private final PeerCallCallback peerCallback;
    private final CallBusCallback callBusCallback;
    private PeerCallInt peerCall;

    public CallBusActor(CallBusCallback callBusCallback, PeerSettings selfSettings, ModuleContext context) {
        super(context);

        this.selfSettings = selfSettings;
        this.callBusCallback = callBusCallback;
        this.peerCallback = new CallbackWrapper(new PeerCallCallback() {
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
            public void onConnectionStarted(long deviceId) {

            }

            @Override
            public void onConnectionEstablished(long deviceId) {

            }

            @Override
            public void onStreamAdded(long deviceId, WebRTCMediaStream stream) {

            }

            @Override
            public void onStreamRemoved(long deviceId, WebRTCMediaStream stream) {

            }
        });
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

        callBusCallback.onBusCreated(peerCall);
    }

    @Override
    public void onBusStarted() {
        super.onBusStarted();
        callBusCallback.onBusStarted(getBusId());
    }

    @Override
    public final void onMessageReceived(@Nullable Integer senderId, @Nullable Long senderDeviceId, byte[] data) {
        if (senderId == null || senderDeviceId == null) {
            return;
        }
        deviceIds.put(senderDeviceId, senderId);

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
            peerCall.onTheirStarted(senderDeviceId);
        } else if (signal instanceof ApiCandidate) {
            ApiCandidate candidate = (ApiCandidate) signal;
            peerCall.onCandidate(senderDeviceId, candidate.getIndex(), candidate.getId(), candidate.getSdp());
        } else if (signal instanceof ApiNeedOffer) {
            ApiNeedOffer needOffer = (ApiNeedOffer) signal;
            deviceIds.put(needOffer.getDevice(), needOffer.getUid());
            peerCall.onAdvertised(needOffer.getDevice(), new PeerSettings(needOffer.getPeerSettings()));
            peerCall.onOfferNeeded(needOffer.getDevice());
            peerCall.onTheirStarted(needOffer.getDevice());
//            if (needOffer.isSilent() != null && !needOffer.isSilent()) {
//                peerCall.onTheirStarted(needOffer.getDevice());
//            }
        } else if (signal instanceof ApiOnAnswer) {
            ApiOnAnswer onAnswer = (ApiOnAnswer) signal;
            deviceIds.put(onAnswer.getDevice(), onAnswer.getUid());
            peerCall.onTheirStarted(onAnswer.getDevice());
        } else {
            if (callBusCallback instanceof CallBusCallbackSlave) {
                CallBusCallbackSlave slaveCallback = (CallBusCallbackSlave) callBusCallback;
                if (signal instanceof ApiSwitchMaster) {
                    slaveCallback.onMasterSwitched(senderId, senderDeviceId);
                } else if (signal instanceof ApiMembersChanged) {
                    ApiMembersChanged membersChanged = (ApiMembersChanged) signal;
                    slaveCallback.onMembersChanged(membersChanged.getAllMembers());
                }
            } else {
                // Nothing?
            }
        }
    }

    public final void sendSignal(long deviceId, ApiWebRTCSignaling signal) {
        int uid = deviceIds.get(deviceId);
        sendSignal(uid, deviceId, signal);
    }

    public final void sendSignal(int uid, long deviceId, ApiWebRTCSignaling signal) {
        Log.d("CallBusActor", "Message Sent: " + signal);
        try {
            sendMessage(uid, deviceId, signal.buildContainer());
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
            sendSignal(signal.getUid(), signal.getDeviceId(), signal.getSignal());
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

        private int uid;
        private long deviceId;
        private ApiWebRTCSignaling signal;

        public SendSignal(int uid, long deviceId, ApiWebRTCSignaling signal) {
            this.uid = uid;
            this.deviceId = deviceId;
            this.signal = signal;
        }

        public int getUid() {
            return uid;
        }

        public long getDeviceId() {
            return deviceId;
        }

        public ApiWebRTCSignaling getSignal() {
            return signal;
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
        public void onConnectionStarted(final long deviceId) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    callCallback.onConnectionStarted(deviceId);
                }
            });
        }

        @Override
        public void onConnectionEstablished(final long deviceId) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    callCallback.onConnectionEstablished(deviceId);
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
