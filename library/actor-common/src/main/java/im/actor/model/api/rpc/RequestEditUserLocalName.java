package im.actor.model.api.rpc;
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

public class RequestEditUserLocalName extends Request<ResponseSeq> {

    public static final int HEADER = 0x60;
    public static RequestEditUserLocalName fromBytes(byte[] data) throws IOException {
        return Bser.parse(new RequestEditUserLocalName(), data);
    }

    private int uid;
    private long accessHash;
    private String name;

    public RequestEditUserLocalName(int uid, long accessHash, String name) {
        this.uid = uid;
        this.accessHash = accessHash;
        this.name = name;
    }

    public RequestEditUserLocalName() {

    }

    public int getUid() {
        return this.uid;
    }

    public long getAccessHash() {
        return this.accessHash;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.uid = values.getInt(1);
        this.accessHash = values.getLong(2);
        this.name = values.getString(3);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, this.uid);
        writer.writeLong(2, this.accessHash);
        if (this.name == null) {
            throw new IOException();
        }
        writer.writeString(3, this.name);
    }

    @Override
    public String toString() {
        String res = "rpc EditUserLocalName{";
        res += "uid=" + this.uid;
        res += ", name=" + this.name;
        res += "}";
        return res;
    }

    @Override
    public int getHeaderKey() {
        return HEADER;
    }
}
