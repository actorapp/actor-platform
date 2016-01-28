package im.actor.core.entity.encryption;

import java.io.IOException;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class PeerSession extends BserObject {

    private long sid;
    private int uid;
    private int ownKeyGroupId;
    private int theirKeyGroupId;
    private long ownPreKeyId;
    private long theirPreKeyId;

    public PeerSession(long sid, int uid, int ownKeyGroupId, int theirKeyGroupId,
                       long ownPreKeyId, long theirPreKeyId) {
        this.sid = sid;
        this.uid = uid;
        this.ownKeyGroupId = ownKeyGroupId;
        this.theirKeyGroupId = theirKeyGroupId;
        this.ownPreKeyId = ownPreKeyId;
        this.theirPreKeyId = theirPreKeyId;
    }

    public PeerSession(byte[] data) throws IOException {
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

    @Override
    public void parse(BserValues values) throws IOException {
        sid = values.getLong(1);
        uid = values.getInt(2);
        ownKeyGroupId = values.getInt(3);
        theirKeyGroupId = values.getInt(4);
        ownPreKeyId = values.getLong(5);
        theirPreKeyId = values.getLong(6);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, sid);
        writer.writeInt(2, uid);
        writer.writeInt(3, ownKeyGroupId);
        writer.writeInt(4, theirKeyGroupId);
        writer.writeLong(5, ownPreKeyId);
        writer.writeLong(6, theirPreKeyId);
    }
}
