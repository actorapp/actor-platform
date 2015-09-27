/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

public class GroupMember {

    @Property("readonly, nonatomic")
    private int uid;

    @Property("readonly, nonatomic")
    private int inviterUid;

    @Property("readonly, nonatomic")
    private long inviteDate;

    @Property("readonly, nonatomic")
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