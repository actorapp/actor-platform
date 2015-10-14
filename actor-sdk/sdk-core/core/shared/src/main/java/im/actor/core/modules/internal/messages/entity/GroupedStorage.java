package im.actor.core.modules.internal.messages.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.entity.Peer;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class GroupedStorage extends BserObject {

    private ArrayList<Peer> groupPeers = new ArrayList<Peer>();
    private ArrayList<Peer> privatePeers = new ArrayList<Peer>();

    public GroupedStorage() {
    }

    public GroupedStorage(byte[] data) throws IOException {
        super.load(data);
    }

    public ArrayList<Peer> getGroupPeers() {
        return groupPeers;
    }

    public ArrayList<Peer> getPrivatePeers() {
        return privatePeers;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        List<byte[]> rawPrivatePeers = values.getRepeatedBytes(1);
        privatePeers.clear();
        for (byte[] b : rawPrivatePeers) {
            privatePeers.add(Peer.fromBytes(b));
        }
        List<byte[]> rawGroupPeers = values.getRepeatedBytes(2);
        for (byte[] b : rawGroupPeers) {
            groupPeers.add(Peer.fromBytes(b));
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        for (Peer peer : privatePeers) {
            writer.writeObject(1, peer);
        }
        for (Peer peer : groupPeers) {
            writer.writeObject(2, peer);
        }
    }
}
