package im.actor.model.entity;

import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.storage.KeyValueItem;

/**
 * Created by ex3ndr on 25.02.15.
 */
public class ContactRecord extends BserObject implements KeyValueItem {

    public static int TYPE_PHONE = 0;
    public static int TYPE_EMAIL = 1;

    public static ContactRecord fromBytes(byte[] data) throws IOException {
        return Bser.parse(new ContactRecord(), data);
    }

    private int id;
    private long accessHash;
    private int recordType;
    private String recordData;
    private String recordTitle;

    public ContactRecord(int id, long accessHash, int recordType, String recordData, String recordTitle) {
        this.id = id;
        this.accessHash = accessHash;
        this.recordType = recordType;
        this.recordData = recordData;
        this.recordTitle = recordTitle;
    }

    public ContactRecord() {

    }

    public int getId() {
        return id;
    }

    public long getAccessHash() {
        return accessHash;
    }

    public int getRecordType() {
        return recordType;
    }

    public String getRecordData() {
        return recordData;
    }

    public String getRecordTitle() {
        return recordTitle;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        id = values.getInt(1);
        accessHash = values.getLong(2);
        recordType = values.getInt(3);
        recordData = values.getString(4);
        recordTitle = values.getString(5);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, id);
        writer.writeLong(2, accessHash);
        writer.writeInt(3, recordType);
        writer.writeString(4, recordData);
        writer.writeString(5, recordTitle);
    }

    @Override
    public long getEngineId() {
        return id;
    }
}
