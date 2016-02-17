package im.actor.core.modules.calls.entity;

public class CallMember {

    private int uid;
    private CallMemberState state;

    public CallMember(int uid, CallMemberState state) {
        this.uid = uid;
        this.state = state;
    }

    public int getUid() {
        return uid;
    }

    public CallMemberState getState() {
        return state;
    }

    public void setState(CallMemberState state) {
        this.state = state;
    }
}
