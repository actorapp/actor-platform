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
import im.actor.model.droidkit.bser.util.SparseArray;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import com.google.j2objc.annotations.ObjectiveCName;
import static im.actor.model.droidkit.bser.Utils.*;
import java.io.IOException;
import im.actor.model.network.parser.*;
import java.util.List;
import java.util.ArrayList;
import im.actor.model.api.*;

public class RequestRegisterApplePush extends Request<ResponseVoid> {

    public static final int HEADER = 0x4c;
    public static RequestRegisterApplePush fromBytes(byte[] data) throws IOException {
        return Bser.parse(new RequestRegisterApplePush(), data);
    }

    private int apnsKey;
    private String token;

    public RequestRegisterApplePush(int apnsKey, @NotNull String token) {
        this.apnsKey = apnsKey;
        this.token = token;
    }

    public RequestRegisterApplePush() {

    }

    public int getApnsKey() {
        return this.apnsKey;
    }

    @NotNull
    public String getToken() {
        return this.token;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.apnsKey = values.getInt(1);
        this.token = values.getString(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, this.apnsKey);
        if (this.token == null) {
            throw new IOException();
        }
        writer.writeString(2, this.token);
    }

    @Override
    public String toString() {
        String res = "rpc RegisterApplePush{";
        res += "}";
        return res;
    }

    @Override
    public int getHeaderKey() {
        return HEADER;
    }
}
