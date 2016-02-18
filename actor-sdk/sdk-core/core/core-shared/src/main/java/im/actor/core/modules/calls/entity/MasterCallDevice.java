package im.actor.core.modules.calls.entity;

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
    private boolean isActive;

    public MasterCallDevice(int uid, long deviceId, boolean isActive) {
        this.uid = uid;
        this.deviceId = deviceId;
        this.isActive = isActive;
    }

    public int getUid() {
        return uid;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}
