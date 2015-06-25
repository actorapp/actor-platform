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

public class RequestEnableInterests extends Request<ResponseVoid> {

    public static final int HEADER = 0x9d;
    public static RequestEnableInterests fromBytes(byte[] data) throws IOException {
        return Bser.parse(new RequestEnableInterests(), data);
    }

    private List<Integer> interests;

    public RequestEnableInterests(@NotNull List<Integer> interests) {
        this.interests = interests;
    }

    public RequestEnableInterests() {

    }

    @NotNull
    public List<Integer> getInterests() {
        return this.interests;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.interests = values.getRepeatedInt(1);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeRepeatedInt(1, this.interests);
    }

    @Override
    public String toString() {
        String res = "rpc EnableInterests{";
        res += "interests=" + this.interests;
        res += "}";
        return res;
    }

    @Override
    public int getHeaderKey() {
        return HEADER;
    }
}
