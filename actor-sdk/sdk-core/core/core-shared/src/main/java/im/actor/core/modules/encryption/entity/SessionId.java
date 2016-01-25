package im.actor.core.modules.encryption.entity;

public final class SessionId {

    private int ownKeyGroupId;
    private int theirKeyGroupId;
    private long ownKeyId0;
    private long theirKeyId0;

    public SessionId(int ownKeyGroupId, long ownKeyId0, int theirKeyGroupId, long theirKeyId0) {
        this.theirKeyGroupId = theirKeyGroupId;
        this.ownKeyId0 = ownKeyId0;
        this.theirKeyId0 = theirKeyId0;
        this.ownKeyGroupId = ownKeyGroupId;
    }

    public int getOwnKeyGroupId() {
        return ownKeyGroupId;
    }

    public int getTheirKeyGroupId() {
        return theirKeyGroupId;
    }

    public long getOwnKeyId0() {
        return ownKeyId0;
    }

    public long getTheirKeyId0() {
        return theirKeyId0;
    }

    @Override
    public String toString() {
        return "SessionId{" +
                "ownKeyGroupId=" + ownKeyGroupId +
                ", theirKeyGroupId=" + theirKeyGroupId +
                ", ownKeyId0=" + ownKeyId0 +
                ", theirKeyId0=" + theirKeyId0 +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SessionId sessionId = (SessionId) o;

        if (ownKeyGroupId != sessionId.ownKeyGroupId) return false;
        if (theirKeyGroupId != sessionId.theirKeyGroupId) return false;
        if (ownKeyId0 != sessionId.ownKeyId0) return false;
        return theirKeyId0 == sessionId.theirKeyId0;
    }

    @Override
    public int hashCode() {
        int result = ownKeyGroupId;
        result = 31 * result + theirKeyGroupId;
        result = 31 * result + (int) (ownKeyId0 ^ (ownKeyId0 >>> 32));
        result = 31 * result + (int) (theirKeyId0 ^ (theirKeyId0 >>> 32));
        return result;
    }
}
