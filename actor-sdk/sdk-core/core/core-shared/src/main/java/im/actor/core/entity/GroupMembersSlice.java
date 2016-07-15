package im.actor.core.entity;

import java.util.ArrayList;

public class GroupMembersSlice {
    
    private ArrayList<Integer> uids;
    private byte[] next;

    public GroupMembersSlice(ArrayList<Integer> uids, byte[] next) {
        this.uids = uids;
        this.next = next;
    }

    public ArrayList<Integer> getUids() {
        return uids;
    }

    public byte[] getNext() {
        return next;
    }
}
