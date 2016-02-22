package im.actor.core.modules.calls.peers.messages;

public class RTCOffer {
    private final long deviceId;
    private final String sdp;

    public RTCOffer(long deviceId, String sdp) {
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
