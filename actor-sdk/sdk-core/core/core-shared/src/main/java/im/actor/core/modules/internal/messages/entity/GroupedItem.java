package im.actor.core.modules.internal.messages.entity;

import java.io.IOException;
import java.util.ArrayList;

import im.actor.core.entity.Peer;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class GroupedItem extends BserObject {

    private String key;
    private String title;
    private ArrayList<Peer> peers = new ArrayList<Peer>();

    public GroupedItem(String key, String title, ArrayList<Peer> peers) {
        this.key = key;
        this.title = title;
        this.peers = peers;
    }

    public GroupedItem(byte[] data) throws IOException {
        load(data);
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<Peer> getPeers() {
        return peers;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        key = values.getString(1);
        title = values.getString(2);
        peers.clear();
        for (byte[] b : values.getRepeatedBytes(3)) {
            peers.add(Peer.fromBytes(b));
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeString(1, key);
        writer.writeString(2, title);
        for (Peer peer : peers) {
            writer.writeObject(3, peer);
        }
    }
}
