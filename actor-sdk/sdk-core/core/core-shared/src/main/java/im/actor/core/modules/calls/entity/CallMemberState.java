package im.actor.core.modules.calls.entity;

import im.actor.core.api.ApiCallMemberState;
import im.actor.core.api.ApiCallMemberStateHolder;

public enum CallMemberState {
    RINGING, CONNECTING, CONNECTED;

    public ApiCallMemberStateHolder toApiState() {
        ApiCallMemberState state;
        Boolean isConnected = null;
        Boolean isConnecting = null;
        Boolean isRinging = null;
        switch (this) {
            case CONNECTED:
                state = ApiCallMemberState.CONNECTED;
                isConnected = true;
                break;
            default:
            case CONNECTING:
                state = ApiCallMemberState.CONNECTING;
                isConnecting = true;
                break;
            case RINGING:
                state = ApiCallMemberState.RINGING;
                isRinging = true;
                break;
        }
        return new ApiCallMemberStateHolder(state, isRinging, isConnected, isConnecting);
    }

    public static CallMemberState fromApi(ApiCallMemberStateHolder stateHolder) {
        switch (stateHolder.getState()) {
            case CONNECTED:
                return CONNECTED;
            case CONNECTING:
                return CONNECTING;
            case RINGING:
                return RINGING;
            default:
                if (stateHolder.fallbackIsConnected() != null && stateHolder.fallbackIsConnected()) {
                    return CONNECTED;
                }
                if (stateHolder.fallbackIsConnecting() != null && stateHolder.fallbackIsConnecting()) {
                    return CONNECTING;
                }
                if (stateHolder.fallbackIsRinging() != null && stateHolder.fallbackIsRinging()) {
                    return RINGING;
                }
                return RINGING;
        }
    }
}
