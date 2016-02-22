package im.actor.core.modules.calls.peers.messages;

import im.actor.core.modules.calls.entity.PeerNodeSettings;

public class RTCAdvertised {

    private final long deviceId;
    private PeerNodeSettings settings;

    public RTCAdvertised(long deviceId, PeerNodeSettings settings) {
        this.deviceId = deviceId;
        this.settings = settings;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public PeerNodeSettings getSettings() {
        return settings;
    }
}
