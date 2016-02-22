package im.actor.core.modules.calls.peers.messages;

public class RTCDispose {

    private long deviceId;

    public RTCDispose(long deviceId) {
        this.deviceId = deviceId;
    }

    public long getDeviceId() {
        return deviceId;
    }
}
