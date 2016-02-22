package im.actor.core.modules.calls.peers.messages;

public class RTCAnswer {

    private long deviceId;
    private final String sdp;

    public RTCAnswer(long deviceId, String sdp) {
        this.deviceId = deviceId;
        this.sdp = sdp;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public String getSdp() {
        return sdp;
    }
}
