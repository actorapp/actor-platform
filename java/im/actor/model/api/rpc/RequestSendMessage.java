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

public class RequestSendMessage extends Request<ResponseSeqDate> {

    public static final int HEADER = 0x5c;
    public static RequestSendMessage fromBytes(byte[] data) throws IOException {
        return Bser.parse(new RequestSendMessage(), data);
    }

    private OutPeer peer;
    private long rid;
    private Message message;

    public RequestSendMessage(OutPeer peer, long rid, Message message) {
        this.peer = peer;
        this.rid = rid;
        this.message = message;
    }

    public RequestSendMessage() {

    }

    public OutPeer getPeer() {
        return this.peer;
    }

    public long getRid() {
        return this.rid;
    }

    public Message getMessage() {
        return this.message;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.peer = values.getObj(1, new OutPeer());
        this.rid = values.getLong(3);
        this.message = Message.fromBytes(values.getBytes(4));
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        if (this.peer == null) {
            throw new IOException();
        }
        writer.writeObject(1, this.peer);
        writer.writeLong(3, this.rid);
        if (this.message == null) {
            throw new IOException();
        }

        writer.writeBytes(4, this.message.buildContainer());
    }

    @Override
    public String toString() {
        String res = "rpc SendMessage{";
        res += "peer=" + this.peer;
        res += ", rid=" + this.rid;
        res += ", message=" + this.message;
        res += "}";
        return res;
    }

    @Override
    public int getHeaderKey() {
        return HEADER;
    }
}
