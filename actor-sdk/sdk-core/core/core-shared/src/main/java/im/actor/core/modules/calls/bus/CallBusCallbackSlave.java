package im.actor.core.modules.calls.bus;

import java.util.List;

import im.actor.core.api.ApiCallMember;

public interface CallBusCallbackSlave extends CallBusCallback {
    void onMasterSwitched(int uid, long deviceId);

    void onMembersChanged(List<ApiCallMember> members);
}
