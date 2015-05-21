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
import static im.actor.model.droidkit.bser.Utils.*;
import java.io.IOException;
import im.actor.model.network.parser.*;
import java.util.List;
import java.util.ArrayList;
import im.actor.model.api.*;

public class RequestEditAvatar extends Request<ResponseEditAvatar> {

    public static final int HEADER = 0x1f;
    public static RequestEditAvatar fromBytes(byte[] data) throws IOException {
        return Bser.parse(new RequestEditAvatar(), data);
    }

    private FileLocation fileLocation;

    public RequestEditAvatar(FileLocation fileLocation) {
        this.fileLocation = fileLocation;
    }

    public RequestEditAvatar() {

    }

    public FileLocation getFileLocation() {
        return this.fileLocation;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.fileLocation = values.getObj(1, new FileLocation());
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        if (this.fileLocation == null) {
            throw new IOException();
        }
        writer.writeObject(1, this.fileLocation);
    }

    @Override
    public String toString() {
        String res = "rpc EditAvatar{";
        res += "fileLocation=" + (this.fileLocation != null ? "set":"empty");
        res += "}";
        return res;
    }

    @Override
    public int getHeaderKey() {
        return HEADER;
    }
}
