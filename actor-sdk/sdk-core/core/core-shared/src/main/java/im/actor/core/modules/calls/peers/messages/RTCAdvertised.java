package im.actor.core.modules.calls.peers.messages;

import im.actor.core.modules.calls.peers.PeerSettings;

public class RTCAdvertised {

    private final long deviceId;
    private PeerSettings settings;

    public RTCAdvertised(long deviceId, PeerSettings settings) {
        this.deviceId = deviceId;
        this.settings = settings;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public PeerSettings getSettings() {
        return settings;
    }
}
