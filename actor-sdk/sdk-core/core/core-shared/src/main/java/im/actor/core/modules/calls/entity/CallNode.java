package im.actor.core.modules.calls.entity;

import im.actor.core.api.ApiPeerSettings;
import im.actor.runtime.function.Predicate;

public class CallNode {

    public static Predicate<CallNode> PREDICATE(final int uid, final long deviceId) {
        return new Predicate<CallNode>() {
            @Override
            public boolean apply(CallNode callNode) {
                return callNode.getMember().getUid() == uid && callNode.getDeviceId() == deviceId;
            }
        };
    }

    public static Predicate<CallNode> PREDICATE(final int uid) {
        return new Predicate<CallNode>() {
            @Override
            public boolean apply(CallNode callNode) {
                return callNode.getMember().getUid() == uid;
            }
        };
    }

    private MasterCallMember member;
    private long deviceId;
    private ApiPeerSettings peerSettings;
    private CallNodeState deviceState;

    public CallNode(MasterCallMember member, long deviceId) {
        this.member = member;
        this.deviceId = deviceId;
        this.deviceState = CallNodeState.PENDING;
        this.peerSettings = null;
    }

    public MasterCallMember getMember() {
        return member;
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

    public CallNodeState getDeviceState() {
        return deviceState;
    }

    public boolean isAnswered() {
        return deviceState == CallNodeState.CONNECTING
                || deviceState == CallNodeState.IN_PROGRESS;
    }

    public boolean isSupportsPreConnection() {
        if (peerSettings == null) {
            return false;
        }
        if (peerSettings.canConnect() != null) {
            return peerSettings.canConnect();
        }
        return false;
    }

    public void setDeviceState(CallNodeState deviceState) {
        this.deviceState = deviceState;
    }

    @Override
    public String toString() {
        return "[" + deviceId + "]";
    }
}
