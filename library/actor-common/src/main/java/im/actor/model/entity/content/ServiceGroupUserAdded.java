/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content;

import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

public class ServiceGroupUserAdded extends ServiceContent {

    public static ServiceGroupUserAdded fromBytes(byte[] data) throws IOException {
        return Bser.parse(new ServiceGroupUserAdded(), data);
    }

    private int addedUid;

    public ServiceGroupUserAdded(int addedUid) {
        super("Member added");
        this.addedUid = addedUid;
    }

    private ServiceGroupUserAdded() {

    }

    public int getAddedUid() {
        return addedUid;
    }

    @Override
    protected ContentType getContentType() {
        return ContentType.SERVICE_ADDED;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        addedUid = values.getInt(10);

    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeInt(10, addedUid);
    }
}
