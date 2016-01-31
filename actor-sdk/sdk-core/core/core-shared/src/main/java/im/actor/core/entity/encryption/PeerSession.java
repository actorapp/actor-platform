package im.actor.core.entity.encryption;

import java.io.IOException;
import java.util.Comparator;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.runtime.function.Predicate;

public class PeerSession extends BserObject {

    public static Predicate<PeerSession> BY_THEIR_GROUP(final int theirKeyGroupId) {
        return new Predicate<PeerSession>() {
            @Override
            public boolean apply(PeerSession session) {
                return session.getTheirKeyGroupId() == theirKeyGroupId;
            }
        };
    }

    public static Predicate<PeerSession> BY_IDS(final int theirKeyGroupId, final long ownPreKeyId, final long theirPreKeyId) {
        return new Predicate<PeerSession>() {
            @Override
            public boolean apply(PeerSession session) {
                return session.getTheirKeyGroupId() == theirKeyGroupId &&
                        session.getOwnPreKeyId() == ownPreKeyId &&
                        session.getTheirPreKeyId() == theirPreKeyId;
            }
        };
    }

    public static final Comparator<PeerSession> COMPARATOR = new Comparator<PeerSession>() {
        @Override
        public int compare(PeerSession lhs, PeerSession rhs) {
            return ByteStrings.compare(lhs.getMasterKey(), rhs.getMasterKey());
        }
    };

    private long sid;
    private int uid;
    private int ownKeyGroupId;
    private int theirKeyGroupId;
    private long ownPreKeyId;
    private long theirPreKeyId;
    private byte[] masterKey;

    public PeerSession(long sid, int uid, int ownKeyGroupId, int theirKeyGroupId,
                       long ownPreKeyId, long theirPreKeyId, byte[] masterKey) {
        this.sid = sid;
        this.uid = uid;
        this.ownKeyGroupId = ownKeyGroupId;
        this.theirKeyGroupId = theirKeyGroupId;
        this.ownPreKeyId = ownPreKeyId;
        this.theirPreKeyId = theirPreKeyId;
        this.masterKey = masterKey;
    }

    public PeerSession(byte[] data) throws IOException {
        load(data);
    }

    public byte[] getMasterKey() {
        return masterKey;
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
        masterKey = values.getBytes(7);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, sid);
        writer.writeInt(2, uid);
        writer.writeInt(3, ownKeyGroupId);
        writer.writeInt(4, theirKeyGroupId);
        writer.writeLong(5, ownPreKeyId);
        writer.writeLong(6, theirPreKeyId);
        writer.writeBytes(7, masterKey);
    }
}
