package im.actor.core.modules.calls.peers.messages;

public class RTCOffer {
    private final long deviceId;
    private final long sessionId;
    private final String sdp;

    public RTCOffer(long deviceId, long sessionId, String sdp) {
        this.deviceId = deviceId;
        this.sessionId = sessionId;
        this.sdp = sdp;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public long getSessionId() {
        return sessionId;
    }

    public String getSdp() {
        return sdp;
    }
}
