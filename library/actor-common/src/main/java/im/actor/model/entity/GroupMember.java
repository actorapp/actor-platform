/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity;

public class GroupMember {

    private int uid;

    private int inviterUid;

    private long inviteDate;

    private boolean isAdministrator;

    public GroupMember(int uid, int inviterUid, long inviteDate,
                       boolean isAdministrator) {
        this.uid = uid;
        this.inviterUid = inviterUid;
        this.inviteDate = inviteDate;
        this.isAdministrator = isAdministrator;
    }

    public GroupMember() {

    }

    public int getUid() {
        return uid;
    }

    public int getInviterUid() {
        return inviterUid;
    }

    public long getInviteDate() {
        return inviteDate;
    }

    public boolean isAdministrator() {
        return isAdministrator;
    }
}