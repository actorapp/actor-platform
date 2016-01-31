package im.actor.core.modules.encryption.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class SessionStorage extends BserObject {

    private long sid;
    private int uid;
    private int ownKeyGroupId;
    private int theirKeyGroupId;
    private long ownPreKeyId;
    private long theirPreKeyId;

    private ArrayList<SessionEphemeralKey> theirKeys;
    private ArrayList<SessionEphemeralKey> ownKeys;

    public SessionStorage(long sid, int uid,
                          int theirKeyGroupId,
                          int ownKeyGroupId,
                          long ownPreKeyId,
                          long theirPreKeyId,
                          ArrayList<SessionEphemeralKey> theirKeys,
                          ArrayList<SessionEphemeralKey> ownKeys) {
        this.sid = sid;
        this.uid = uid;
        this.theirKeyGroupId = theirKeyGroupId;
        this.ownKeyGroupId = ownKeyGroupId;
        this.ownPreKeyId = ownPreKeyId;
        this.theirPreKeyId = theirPreKeyId;
        this.theirKeys = new ArrayList<>(theirKeys);
        this.ownKeys = new ArrayList<>(ownKeys);
    }

    public SessionStorage(byte[] data) throws IOException {
        load(data);
    }

    public long getSid() {
        return sid;
    }

    public int getUid() {
        return uid;
    }

    public int getOwnKeyGroupId() {
        return ownKeyGroupId;
    }

    public int getTheirKeyGroupId() {
        return theirKeyGroupId;
    }

    public long getOwnPreKeyId() {
        return ownPreKeyId;
    }

    public long getTheirPreKeyId() {
        return theirPreKeyId;
    }

    public ArrayList<SessionEphemeralKey> getTheirKeys() {
        return theirKeys;
    }

    public ArrayList<SessionEphemeralKey> getOwnKeys() {
        return ownKeys;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        sid = values.getLong(1);
        uid = values.getInt(2);
        ownKeyGroupId = values.getInt(3);
        theirKeyGroupId = values.getInt(4);
        ownPreKeyId = values.getLong(5);
        theirPreKeyId = values.getLong(6);

        theirKeys = new ArrayList<>();
        List<byte[]> theirEphemeral = values.getRepeatedBytes(7);
        for (byte[] b : theirEphemeral) {
            theirKeys.add(new SessionEphemeralKey(b));
        }

        ownKeys = new ArrayList<>();
        List<byte[]> ownEphemeral = values.getRepeatedBytes(8);
        for (byte[] b : ownEphemeral) {
            theirKeys.add(new SessionEphemeralKey(b));
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, sid);
        writer.writeInt(2, uid);
        writer.writeInt(3, ownKeyGroupId);
        writer.writeInt(4, theirKeyGroupId);
        writer.writeLong(5, ownPreKeyId);
        writer.writeLong(6, theirPreKeyId);
        writer.writeRepeatedObj(7, theirKeys);
        writer.writeRepeatedObj(8, ownKeys);
    }
}
