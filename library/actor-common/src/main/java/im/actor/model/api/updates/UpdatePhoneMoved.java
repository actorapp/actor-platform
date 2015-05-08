package im.actor.model.api.updates;
/*
 *  Generated by the Actor API Scheme generator.  DO NOT EDIT!
 */

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserParser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.droidkit.bser.DataInput;
import im.actor.model.droidkit.bser.DataOutput;
import static im.actor.model.droidkit.bser.Utils.*;
import java.io.IOException;
import im.actor.model.network.parser.*;
import java.util.List;
import java.util.ArrayList;
import im.actor.model.api.*;

public class UpdatePhoneMoved extends Update {

    public static final int HEADER = 0x65;
    public static UpdatePhoneMoved fromBytes(byte[] data) throws IOException {
        return Bser.parse(new UpdatePhoneMoved(), data);
    }

    private int phoneId;
    private int uid;

    public UpdatePhoneMoved(int phoneId, int uid) {
        this.phoneId = phoneId;
        this.uid = uid;
    }

    public UpdatePhoneMoved() {

    }

    public int getPhoneId() {
        return this.phoneId;
    }

    public int getUid() {
        return this.uid;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.phoneId = values.getInt(1);
        this.uid = values.getInt(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, this.phoneId);
        writer.writeInt(2, this.uid);
    }

    @Override
    public String toString() {
        String res = "update PhoneMoved{";
        res += "phoneId=" + this.phoneId;
        res += ", uid=" + this.uid;
        res += "}";
        return res;
    }

    @Override
    public int getHeaderKey() {
        return HEADER;
    }
}
