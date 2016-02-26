package im.actor.core.modules.calls.peers.messages;

public class RTCAnswer {

    private long deviceId;
    private long sessionId;
    private final String sdp;

    public RTCAnswer(long deviceId, long sessionId, String sdp) {
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
