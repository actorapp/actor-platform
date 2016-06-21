package im.actor.core.modules.calls.peers.messages;

public class RTCCandidate {

    private long deviceId;
    private long sessionId;
    private final int mdpIndex;
    private final String id;
    private final String sdp;

    public RTCCandidate(long deviceId,long sessionId, int mdpIndex, String id, String sdp) {
        this.deviceId = deviceId;
        this.sessionId = sessionId;
        this.mdpIndex = mdpIndex;
        this.id = id;
        this.sdp = sdp;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public long getSessionId() {
        return sessionId;
    }

    public int getMdpIndex() {
        return mdpIndex;
    }

    public String getId() {
        return id;
    }

    public String getSdp() {
        return sdp;
    }
}
