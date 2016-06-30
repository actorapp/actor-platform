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

public class RequestJoinCall extends Request<ResponseVoid> {

    public static final int HEADER = 0xa7b;
    public static RequestJoinCall fromBytes(byte[] data) throws IOException {
        return Bser.parse(new RequestJoinCall(), data);
    }

    private long callId;

    public RequestJoinCall(long callId) {
        this.callId = callId;
    }

    public RequestJoinCall() {

    }

    public long getCallId() {
        return this.callId;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.callId = values.getLong(1);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, this.callId);
    }

    @Override
    public String toString() {
        String res = "rpc JoinCall{";
        res += "callId=" + this.callId;
        res += "}";
        return res;
    }

    @Override
    public int getHeaderKey() {
        return HEADER;
    }
}
