package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import java.util.ArrayList;

public class GroupMembersSlice {

    @Property("readonly, nonatomic")
    private ArrayList<GroupMember> members;
    @Property("readonly, nonatomic")
    private byte[] next;

    public GroupMembersSlice(ArrayList<GroupMember> members, byte[] next) {
        this.members = members;
        this.next = next;
    }

    public ArrayList<GroupMember> getMembers() {
        return members;
    }

    public byte[] getNext() {
        return next;
    }
}
