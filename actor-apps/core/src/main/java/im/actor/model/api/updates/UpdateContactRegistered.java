package im.actor.model.api.updates;
/*
 *  Generated by the Actor API Scheme generator.  DO NOT EDIT!
 */

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import im.actor.model.network.parser.*;

public class UpdateContactRegistered extends Update {

    public static final int HEADER = 0x5;
    public static UpdateContactRegistered fromBytes(byte[] data) throws IOException {
        return Bser.parse(new UpdateContactRegistered(), data);
    }

    private int uid;
    private boolean isSilent;
    private long date;
    private long rid;

    public UpdateContactRegistered(int uid, boolean isSilent, long date, long rid) {
        this.uid = uid;
        this.isSilent = isSilent;
        this.date = date;
        this.rid = rid;
    }

    public UpdateContactRegistered() {

    }

    public int getUid() {
        return this.uid;
    }

    public boolean isSilent() {
        return this.isSilent;
    }

    public long getDate() {
        return this.date;
    }

    public long getRid() {
        return this.rid;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.uid = values.getInt(1);
        this.isSilent = values.getBool(2);
        this.date = values.getLong(3);
        this.rid = values.getLong(4);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, this.uid);
        writer.writeBool(2, this.isSilent);
        writer.writeLong(3, this.date);
        writer.writeLong(4, this.rid);
    }

    @Override
    public String toString() {
        String res = "update ContactRegistered{";
        res += "uid=" + this.uid;
        res += ", isSilent=" + this.isSilent;
        res += ", date=" + this.date;
        res += "}";
        return res;
    }

    @Override
    public int getHeaderKey() {
        return HEADER;
    }
}
