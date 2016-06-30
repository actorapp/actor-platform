package im.actor.core.modules.calls.peers.messages;

public class RTCStart {
    private long deviceId;

    public RTCStart(long deviceId) {
        this.deviceId = deviceId;
    }

    public long getDeviceId() {
        return deviceId;
    }
}
