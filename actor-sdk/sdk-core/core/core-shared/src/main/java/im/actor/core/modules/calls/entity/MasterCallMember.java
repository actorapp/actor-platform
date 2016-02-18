package im.actor.core.modules.calls.entity;

import java.util.ArrayList;

import im.actor.core.entity.GroupMember;
import im.actor.runtime.function.Function;
import im.actor.runtime.function.Predicate;

public class MasterCallMember extends CallMember {

    public static Function<GroupMember, MasterCallMember> FROM_MEMBER = new Function<GroupMember, MasterCallMember>() {
        @Override
        public MasterCallMember apply(GroupMember groupMember) {
            return new MasterCallMember(groupMember.getUid(), CallMemberState.RINGING);
        }
    };

    public static Predicate<MasterCallMember> PREDICATE(final int uid, final long deviceId) {
        return new Predicate<MasterCallMember>() {
            @Override
            public boolean apply(MasterCallMember masterCallMember) {
                return masterCallMember.getUid() == uid && masterCallMember.getDeviceId().contains(deviceId);
            }
        };
    }

    public static Predicate<MasterCallMember> PREDICATE(final int uid) {
        return new Predicate<MasterCallMember>() {
            @Override
            public boolean apply(MasterCallMember masterCallMember) {
                return masterCallMember.getUid() == uid;
            }
        };
    }

    private ArrayList<Long> deviceId = new ArrayList<>();

    public MasterCallMember(int uid, CallMemberState state) {
        super(uid, state);
    }

    public ArrayList<Long> getDeviceId() {
        return deviceId;
    }
}
