package im.actor.core.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class Reaction extends BserObject {

    public static Reaction fromBytes(byte[] data) throws IOException {
        return Bser.parse(new Reaction(), data);
    }

    private String code;
    private List<Integer> uids = new ArrayList<Integer>();

    public Reaction(String code, List<Integer> uids) {
        this.code = code;
        this.uids = uids;
    }

    private Reaction() {

    }

    public String getCode() {
        return code;
    }

    public List<Integer> getUids() {
        return uids;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        code = values.getString(1);
        uids.clear();
        uids.addAll(values.getRepeatedInt(2));
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeString(1, code);
        writer.writeRepeatedInt(2, uids);
    }
}
