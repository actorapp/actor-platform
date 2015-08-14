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

public class RequestEditGroupAbout extends Request<ResponseSeqDate> {

    public static final int HEADER = 0xd5;
    public static RequestEditGroupAbout fromBytes(byte[] data) throws IOException {
        return Bser.parse(new RequestEditGroupAbout(), data);
    }

    private ApiGroupOutPeer groupPeer;
    private long rid;
    private String about;

    public RequestEditGroupAbout(@NotNull ApiGroupOutPeer groupPeer, long rid, @Nullable String about) {
        this.groupPeer = groupPeer;
        this.rid = rid;
        this.about = about;
    }

    public RequestEditGroupAbout() {

    }

    @NotNull
    public ApiGroupOutPeer getGroupPeer() {
        return this.groupPeer;
    }

    public long getRid() {
        return this.rid;
    }

    @Nullable
    public String getAbout() {
        return this.about;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.groupPeer = values.getObj(1, new ApiGroupOutPeer());
        this.rid = values.getLong(2);
        this.about = values.optString(3);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        if (this.groupPeer == null) {
            throw new IOException();
        }
        writer.writeObject(1, this.groupPeer);
        writer.writeLong(2, this.rid);
        if (this.about != null) {
            writer.writeString(3, this.about);
        }
    }

    @Override
    public String toString() {
        String res = "rpc EditGroupAbout{";
        res += "groupPeer=" + this.groupPeer;
        res += ", rid=" + this.rid;
        res += "}";
        return res;
    }

    @Override
    public int getHeaderKey() {
        return HEADER;
    }
}
