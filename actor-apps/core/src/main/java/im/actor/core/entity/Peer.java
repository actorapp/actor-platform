/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import java.io.IOException;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class Peer extends BserObject {

    public static final BserCreator<Peer> CREATOR = new BserCreator<Peer>() {
        @Override
        public Peer createInstance() {
            return new Peer();
        }
    };

    public static Peer fromBytes(byte[] data) throws IOException {
        return Bser.parse(new Peer(), data);
    }

    public static Peer fromUniqueId(long uid) {
        int id = (int) (uid & 0xFFFFFFFFL);
        int type = (int) ((uid >> 32) & 0xFFFFFFFFL);

        switch (type) {
            default:
            case 0:
                return new Peer(PeerType.PRIVATE, id);
            case 1:
                return new Peer(PeerType.GROUP, id);
        }
    }

    public static Peer user(int uid) {
        return new Peer(PeerType.PRIVATE, uid);
    }

    public static Peer group(int gid) {
        return new Peer(PeerType.GROUP, gid);
    }

    @Property("readonly, nonatomic")
    private PeerType peerType;
    @Property("readonly, nonatomic")
    private int peerId;

    public Peer(PeerType peerType, int peerId) {
        this.peerType = peerType;
        this.peerId = peerId;
    }

    private Peer() {

    }

    public long getUnuqueId() {
        int type;
        switch (peerType) {
            default:
            case PRIVATE:
                type = 0;
                break;
            case GROUP:
                type = 1;
                break;
        }
        return ((long) peerId & 0xFFFFFFFFL) + (((long) type & 0xFFFFFFFFL) << 32);
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

    @Override
    public void parse(BserValues values) throws IOException {
        peerId = values.getInt(1);
        switch (values.getInt(2)) {
            default:
            case 1:
                peerType = PeerType.PRIVATE;
                break;
            case 3:
                peerType = PeerType.GROUP;
                break;
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, peerId);
        switch (peerType) {
            default:
            case PRIVATE:
                writer.writeInt(2, 1);
                break;
            case GROUP:
                writer.writeInt(2, 3);
                break;
        }
    }

    @Override
    public String toString() {
        return "{type:" + peerType + ", id:" + peerId + "}";
    }

    public String toIdString() {
        return peerType + "_" + peerId;
    }
}
