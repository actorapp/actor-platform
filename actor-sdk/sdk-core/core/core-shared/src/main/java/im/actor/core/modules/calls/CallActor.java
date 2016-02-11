package im.actor.core.modules.calls;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import im.actor.core.api.ApiWebRTCSignaling;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.eventbus.EventBusActor;
import im.actor.runtime.Log;
import im.actor.runtime.WebRTC;
import im.actor.runtime.function.Function;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

public class CallActor extends EventBusActor {

    private static final String TAG = "CallActor";

    public CallActor(ModuleContext context) {
        super(context);
    }

    public CallActor(String busId, ModuleContext context) {
        super(busId, context);
    }

    public void onSignalingMessage(int fromUid, long fromDeviceId, ApiWebRTCSignaling signaling) {

    }

    @Override
    public final void onMessageReceived(@Nullable Integer senderId, @Nullable Long senderDeviceId, byte[] data) {
        // Ignoring messages without sender
        if (senderId == null || senderDeviceId == null) {
            return;
        }

        ApiWebRTCSignaling signaling;
        try {
            signaling = ApiWebRTCSignaling.fromBytes(data);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "onMessageReceived:ignoring");
            return;
        }

        Log.d(TAG, "onMessageReceived: " + signaling);
        onSignalingMessage(senderId, senderDeviceId, signaling);
    }

    protected Promise<PeerConnection> createConnection(final int userId, final long deviceId) {
        return WebRTC.createPeerConnection().map(new Function<WebRTCPeerConnection, PeerConnection>() {
            @Override
            public PeerConnection apply(WebRTCPeerConnection webRTCPeerConnection) {
                return new PeerConnection(userId, deviceId, webRTCPeerConnection);
            }
        });
    }

    protected static class PeerConnection {

        private int uid;
        private long deviceId;
        private WebRTCPeerConnection webRTCPeerConnection;

        public PeerConnection(int uid, long deviceId, WebRTCPeerConnection webRTCPeerConnection) {
            this.uid = uid;
            this.deviceId = deviceId;
            this.webRTCPeerConnection = webRTCPeerConnection;
        }

        public int getUid() {
            return uid;
        }

        public long getDeviceId() {
            return deviceId;
        }

        public WebRTCPeerConnection getWebRTCPeerConnection() {
            return webRTCPeerConnection;
        }
    }
}