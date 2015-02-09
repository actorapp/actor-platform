package im.actor.model.entity;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class Peer {
    private final PeerType peerType;
    private final int peerId;

    public Peer(PeerType peerType, int peerId) {
        this.peerType = peerType;
        this.peerId = peerId;
    }

    public long getUid() {
        int type;
        switch (peerType) {
            default:
            case PRIVATE:
                type = 0;
                break;
            case GROUP:
                type = 1;
                break;
            case EMAIL:
                type = 2;
                break;
        }
        return peerId + ((long) type << 32);
    }

    public PeerType getPeerType() {
        return peerType;
    }

    public int getPeerId() {
        return peerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Peer peer = (Peer) o;

        if (peerId != peer.peerId) return false;
        if (peerType != peer.peerType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = peerType.hashCode();
        result = 31 * result + peerId;
        return result;
    }
}
