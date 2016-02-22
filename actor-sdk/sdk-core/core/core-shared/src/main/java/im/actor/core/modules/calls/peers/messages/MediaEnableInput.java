package im.actor.core.modules.calls.peers.messages;

public class MediaEnableInput {

    private final boolean isEnabled;
    private final boolean isForAll;
    private final long deviceId;

    public MediaEnableInput(boolean isEnabled) {
        this.isEnabled = isEnabled;
        this.isForAll = true;
        this.deviceId = 0;
    }

    public MediaEnableInput(boolean isEnabled, long deviceId) {
        this.isEnabled = isEnabled;
        this.deviceId = deviceId;
        this.isForAll = false;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isForAll() {
        return isForAll;
    }

    public long getDeviceId() {
        return deviceId;
    }
}
