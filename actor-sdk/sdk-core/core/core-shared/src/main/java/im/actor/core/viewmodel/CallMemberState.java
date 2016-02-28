package im.actor.core.viewmodel;

import im.actor.core.api.ApiCallMemberState;
import im.actor.core.api.ApiCallMemberStateHolder;

public enum CallMemberState {
    RINGING, RINGING_REACHED, CONNECTING, IN_PROGRESS, ENDED;

    public static CallMemberState from(ApiCallMemberStateHolder state) {
        switch (state.getState()) {
            case RINGING:
                return CallMemberState.RINGING;
            case RINGING_REACHED:
                return CallMemberState.RINGING_REACHED;
            case CONNECTING:
                return CallMemberState.CONNECTING;
            case CONNECTED:
                return CallMemberState.IN_PROGRESS;
            case ENDED:
                return CallMemberState.ENDED;
            default:
                if (state.fallbackIsRingingReached() != null && state.fallbackIsRingingReached()) {
                    return CallMemberState.RINGING_REACHED;
                }
                if (state.fallbackIsEnded() != null && state.fallbackIsEnded()) {
                    return CallMemberState.ENDED;
                }
                if (state.fallbackIsRinging() != null && state.fallbackIsRinging()) {
                    return CallMemberState.RINGING;
                }

                if (state.fallbackIsConnecting() != null && state.fallbackIsConnecting()) {
                    return CallMemberState.CONNECTING;
                }

                if (state.fallbackIsConnected() != null && state.fallbackIsConnected()) {
                    return CallMemberState.IN_PROGRESS;
                }
                return CallMemberState.RINGING;
        }
    }
}
