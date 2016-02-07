package im.actor.core.viewmodel;

import java.util.ArrayList;

import im.actor.core.entity.CallState;
import im.actor.core.entity.Peer;
import im.actor.runtime.mvvm.ValueModel;

public class CallModel {

    private final long callId;
    private final Peer peer;
    private final ValueModel<CallState> state;
    private final ValueModel<Long> callStarted;
    private final ValueModel<ArrayList<Integer>> activeMembers;

    public CallModel(long callId, Peer peer, ArrayList<Integer> activeMembers, CallState state) {
        this.callId = callId;
        this.peer = peer;
        this.state = new ValueModel<>("calls." + callId + ".state", state);
        this.callStarted = new ValueModel<>("calls." + callId + ".started", null);
        this.activeMembers = new ValueModel<>("calls." + callId + ".members", new ArrayList<>(activeMembers));
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

    public Peer getPeer() {
        return peer;
    }

    public ValueModel<ArrayList<Integer>> getActiveMembers() {
        return activeMembers;
    }
}