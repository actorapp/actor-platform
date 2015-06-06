/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.compat;

import java.io.IOException;

import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

public class ObsoleteGroupMember extends BserObject {
    private int uid;

    private int inviterUid;

    private long inviteDate;

    private boolean isAdministrator;

    public ObsoleteGroupMember(byte[] data) throws IOException {
        load(data);
    }

    public ObsoleteGroupMember(BserValues values) throws IOException {
        parse(values);
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

    @Override
    public void parse(BserValues values) throws IOException {
        uid = values.getInt(1);
        inviterUid = values.getInt(2);
        inviteDate = values.getLong(3);
        isAdministrator = values.getBool(4);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        throw new UnsupportedOperationException();
    }
}
