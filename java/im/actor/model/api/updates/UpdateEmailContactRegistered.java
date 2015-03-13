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

public class UpdateEmailContactRegistered extends Update {

    public static final int HEADER = 0x78;
    public static UpdateEmailContactRegistered fromBytes(byte[] data) throws IOException {
        return Bser.parse(new UpdateEmailContactRegistered(), data);
    }

    private int emailId;
    private int uid;

    public UpdateEmailContactRegistered(int emailId, int uid) {
        this.emailId = emailId;
        this.uid = uid;
    }

    public UpdateEmailContactRegistered() {

    }

    public int getEmailId() {
        return this.emailId;
    }

    public int getUid() {
        return this.uid;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.emailId = values.getInt(1);
        this.uid = values.getInt(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, this.emailId);
        writer.writeInt(2, this.uid);
    }

    @Override
    public String toString() {
        String res = "update EmailContactRegistered{";
        res += "emailId=" + this.emailId;
        res += ", uid=" + this.uid;
        res += "}";
        return res;
    }

    @Override
    public int getHeaderKey() {
        return HEADER;
    }
}
