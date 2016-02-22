package im.actor.core.modules.calls.peers.messages;

public class RTCNeedOffer {

    private final long deviceId;

    public RTCNeedOffer(long deviceId) {
        this.deviceId = deviceId;
    }

    public long getDeviceId() {
        return deviceId;
    }
}
