package im.actor.core.modules.calls.peers.messages;

public class RTCMediaStateUpdated {
    
    private long deviceId;
    private boolean isAudioEnabled;
    private boolean isVideoEnabled;

    public RTCMediaStateUpdated(long deviceId, boolean isAudioEnabled, boolean isVideoEnabled) {
        this.deviceId = deviceId;
        this.isAudioEnabled = isAudioEnabled;
        this.isVideoEnabled = isVideoEnabled;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public boolean isAudioEnabled() {
        return isAudioEnabled;
    }

    public boolean isVideoEnabled() {
        return isVideoEnabled;
    }
}
