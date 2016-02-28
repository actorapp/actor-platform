package im.actor.core.modules.calls.peers.messages;

public class RTCCloseSession {

    private long deviceId;
    private long sessionId;

    public RTCCloseSession(long deviceId, long sessionId) {
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
