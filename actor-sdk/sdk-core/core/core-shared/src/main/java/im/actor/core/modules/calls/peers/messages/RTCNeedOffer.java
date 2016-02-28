package im.actor.core.modules.calls.peers.messages;

public class RTCNeedOffer {

    private final long deviceId;
    private final long sessionId;

    public RTCNeedOffer(long deviceId,long sessionId) {
        this.deviceId = deviceId;
        this.sessionId = sessionId;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public long getSessionId() {
        return sessionId;
    }
}
