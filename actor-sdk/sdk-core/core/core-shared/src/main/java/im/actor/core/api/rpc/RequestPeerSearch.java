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

public class RequestPeerSearch extends Request<ResponsePeerSearch> {

    public static final int HEADER = 0xe9;
    public static RequestPeerSearch fromBytes(byte[] data) throws IOException {
        return Bser.parse(new RequestPeerSearch(), data);
    }

    private List<ApiSearchCondition> query;

    public RequestPeerSearch(@NotNull List<ApiSearchCondition> query) {
        this.query = query;
    }

    public RequestPeerSearch() {

    }

    @NotNull
    public List<ApiSearchCondition> getQuery() {
        return this.query;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.query = new ArrayList<ApiSearchCondition>();
        for (byte[] b : values.getRepeatedBytes(1)) {
            query.add(ApiSearchCondition.fromBytes(b));
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        for (ApiSearchCondition i : this.query) {
            writer.writeBytes(1, i.buildContainer());
        }
    }

    @Override
    public String toString() {
        String res = "rpc PeerSearch{";
        res += "query=" + this.query;
        res += "}";
        return res;
    }

    @Override
    public int getHeaderKey() {
        return HEADER;
    }
}
