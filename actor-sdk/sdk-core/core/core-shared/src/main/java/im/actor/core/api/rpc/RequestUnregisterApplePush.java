package im.actor.core.api.rpc;
/*
 *  Generated by the Actor API Scheme generator.  DO NOT EDIT!
 */

import im.actor.runtime.bser.*;
import im.actor.runtime.collections.*;
import static im.actor.runtime.bser.Utils.*;
import im.actor.core.network.parser.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import com.google.j2objc.annotations.ObjectiveCName;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import im.actor.core.api.*;

public class RequestUnregisterApplePush extends Request<ResponseVoid> {

    public static final int HEADER = 0xa48;
    public static RequestUnregisterApplePush fromBytes(byte[] data) throws IOException {
        return Bser.parse(new RequestUnregisterApplePush(), data);
    }

    private String token;

    public RequestUnregisterApplePush(@NotNull String token) {
        this.token = token;
    }

    public RequestUnregisterApplePush() {

    }

    @NotNull
    public String getToken() {
        return this.token;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.token = values.getString(1);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        if (this.token == null) {
            throw new IOException();
        }
        writer.writeString(1, this.token);
    }

    @Override
    public String toString() {
        String res = "rpc UnregisterApplePush{";
        res += "token=" + this.token;
        res += "}";
        return res;
    }

    @Override
    public int getHeaderKey() {
        return HEADER;
    }
}
