/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.android.modules.call;

public class CurrentCall {

    private long rid;
    private int callUser;
    private CallState callState;

    public CurrentCall(long rid, int callUser, CallState callState) {
        this.rid = rid;
        this.callUser = callUser;
        this.callState = callState;
    }

    public long getRid() {
        return rid;
    }

    public int getCallUser() {
        return callUser;
    }

    public CallState getCallState() {
        return callState;
    }
}
