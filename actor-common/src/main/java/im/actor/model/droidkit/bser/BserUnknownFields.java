package im.actor.model.droidkit.bser;

import java.util.ArrayList;

/**
 * Created by ex3ndr on 08.03.15.
 */
public class BserUnknownFields {

    private ArrayList<BserUnknownField> fields = new ArrayList<BserUnknownField>();

    public void add(int id, int type, byte[] field) {
        fields.add(new BserUnknownField(id, type, field));
    }

    public void add(int id, int type, long value) {
        fields.add(new BserUnknownField(id, type, Utils.longToBytes(value)));
    }
}