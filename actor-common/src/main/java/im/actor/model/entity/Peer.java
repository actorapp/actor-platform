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
}
