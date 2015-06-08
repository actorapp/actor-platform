/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.compat;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

public class ObsoleteContactRecord extends BserObject {

    private int id;
    private long accessHash;
    private int recordType;
    private String recordData;
    private String recordTitle;

    public ObsoleteContactRecord(@NotNull byte[] data) throws IOException {
        load(data);
    }

    public ObsoleteContactRecord(@NotNull BserValues values) throws IOException {
        parse(values);
    }

    public int getId() {
        return id;
    }

    public long getAccessHash() {
        return accessHash;
    }

    public int getRecordType() {
        return recordType;
    }

    public String getRecordData() {
        return recordData;
    }

    public String getRecordTitle() {
        return recordTitle;
    }

    @Override
    public void parse(@NotNull BserValues values) throws IOException {
        id = values.getInt(1);
        accessHash = values.getLong(2);
        recordType = values.getInt(3);
        recordData = values.getString(4);
        recordTitle = values.getString(5);
    }

    @Override
    public void serialize(@NotNull BserWriter writer) throws IOException {
        throw new UnsupportedOperationException();
    }
}
