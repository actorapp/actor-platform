package im.actor.core.viewmodel;

import im.actor.core.entity.CallState;
import im.actor.runtime.mvvm.ValueModel;

public class CallViewModel {

    private long callId;
    private ValueModel<CallState> state;
    private ValueModel<Long> callStarted;

    public CallViewModel(long callId, CallState state) {
        this.callId = callId;
        this.state = new ValueModel<>("calls." + callId + ".state", state);
        this.callStarted = new ValueModel<>("calls." + callId + ".started", null);
    }

    public long getCallId() {
        return callId;
    }

    public ValueModel<CallState> getState() {
        return state;
    }

    public ValueModel<Long> getCallStarted() {
        return callStarted;
    }
}
