/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

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

public class RequestEditParameter extends Request<ResponseSeq> {

    public static final int HEADER = 0x80;
    public static RequestEditParameter fromBytes(byte[] data) throws IOException {
        return Bser.parse(new RequestEditParameter(), data);
    }

    private String key;
    private String value;

    public RequestEditParameter(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public RequestEditParameter() {

    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.key = values.getString(1);
        this.value = values.getString(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        if (this.key == null) {
            throw new IOException();
        }
        writer.writeString(1, this.key);
        if (this.value == null) {
            throw new IOException();
        }
        writer.writeString(2, this.value);
    }

    @Override
    public String toString() {
        String res = "rpc EditParameter{";
        res += "}";
        return res;
    }

    @Override
    public int getHeaderKey() {
        return HEADER;
    }
}
