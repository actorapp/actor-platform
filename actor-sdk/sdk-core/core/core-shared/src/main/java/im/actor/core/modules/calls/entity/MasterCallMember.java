package im.actor.core.modules.calls.entity;

import im.actor.runtime.function.Predicate;

public class MasterCallMember {


    public static Predicate<MasterCallMember> IS_ENDED = new Predicate<MasterCallMember>() {
        @Override
        public boolean apply(MasterCallMember masterCallMember) {
            return masterCallMember.getState() == MasterCallMemberState.ENDED;
        }
    };

    public static Predicate<MasterCallMember> IS_RINGING = new Predicate<MasterCallMember>() {
        @Override
        public boolean apply(MasterCallMember masterCallMember) {
            return masterCallMember.getState() == MasterCallMemberState.RINGING_REACHED ||
                    masterCallMember.getState() == MasterCallMemberState.RINGING;
        }
    };

    public static Predicate<MasterCallMember> IS_IN_PROGRESS = new Predicate<MasterCallMember>() {
        @Override
        public boolean apply(MasterCallMember masterCallMember) {
            return masterCallMember.getState() == MasterCallMemberState.IN_PROGRESS;
        }
    };

    public static Predicate<MasterCallMember> IS_CONNECTING = new Predicate<MasterCallMember>() {
        @Override
        public boolean apply(MasterCallMember masterCallMember) {
            return masterCallMember.getState() == MasterCallMemberState.CONNECTING;
        }
    };

    public static Predicate<MasterCallMember> PREDICATE(final int uid) {
        return new Predicate<MasterCallMember>() {
            @Override
            public boolean apply(MasterCallMember masterCallMember) {
                return masterCallMember.getUid() == uid;
            }
        };
    }

    private int uid;
    private MasterCallMemberState state;

    public MasterCallMember(int uid, MasterCallMemberState state) {
        this.uid = uid;
        this.state = state;
    }

    public int getUid() {
        return uid;
    }

    public MasterCallMemberState getState() {
        return state;
    }

    public void setState(MasterCallMemberState state) {
        this.state = state;
    }
}
