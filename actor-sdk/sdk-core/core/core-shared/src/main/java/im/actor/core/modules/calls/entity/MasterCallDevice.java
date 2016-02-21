package im.actor.core.modules.calls.entity;

import im.actor.core.api.ApiPeerSettings;
import im.actor.runtime.function.Predicate;

public class MasterCallDevice {

    public static Predicate<MasterCallDevice> PREDICATE(final int uid, final long deviceId) {
        return new Predicate<MasterCallDevice>() {
            @Override
            public boolean apply(MasterCallDevice masterCallDevice) {
                return masterCallDevice.getUid() == uid && masterCallDevice.getDeviceId() == deviceId;
            }
        };
    }

    public static Predicate<MasterCallDevice> PREDICATE(final int uid) {
        return new Predicate<MasterCallDevice>() {
            @Override
            public boolean apply(MasterCallDevice masterCallDevice) {
                return masterCallDevice.getUid() == uid;
            }
        };
    }

    private int uid;
    private long deviceId;
    private ApiPeerSettings peerSettings;
    private MasterCallDeviceState deviceState;

    public MasterCallDevice(int uid, long deviceId) {
        this.uid = uid;
        this.deviceId = deviceId;
        this.deviceState = MasterCallDeviceState.PENDING;
        this.peerSettings = null;
    }

    public int getUid() {
        return uid;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public ApiPeerSettings getPeerSettings() {
        return peerSettings;
    }

    public void setPeerSettings(ApiPeerSettings peerSettings) {
        this.peerSettings = peerSettings;
    }

    public MasterCallDeviceState getDeviceState() {
        return deviceState;
    }

    public boolean isAnswered() {
        return deviceState == MasterCallDeviceState.CONNECTING
                || deviceState == MasterCallDeviceState.IN_PROGRESS;
    }

    public void setDeviceState(MasterCallDeviceState deviceState) {
        this.deviceState = deviceState;
    }
}
