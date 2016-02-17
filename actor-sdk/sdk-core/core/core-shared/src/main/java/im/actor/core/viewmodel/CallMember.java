package im.actor.core.viewmodel;

import com.google.j2objc.annotations.Property;

public class CallMember {

    @Property("nonatomic, readonly")
    private int uid;
    @Property("nonatomic, readonly")
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

    @Override
    public String toString() {
        return uid + " | " + state.name();
    }
}