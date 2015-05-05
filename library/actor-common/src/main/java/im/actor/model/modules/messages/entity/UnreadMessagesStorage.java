/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.messages.entity;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.entity.Peer;

public class UnreadMessagesStorage extends BserObject {

    public static UnreadMessagesStorage fromBytes(byte[] data) throws IOException {
        return Bser.parse(new UnreadMessagesStorage(), data);
    }

    private HashMap<Peer, HashSet<UnreadMessage>> unreadMessages = new HashMap<Peer, HashSet<UnreadMessage>>();

    public HashSet<UnreadMessage> getUnread(Peer peer) {
        if (!unreadMessages.containsKey(peer)) {
            unreadMessages.put(peer, new HashSet<UnreadMessage>());
        }
        return unreadMessages.get(peer);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        List<byte[]> data = values.getRepeatedBytes(1);
        for (byte[] d : data) {
            try {
                UnreadMessage u = UnreadMessage.fromBytes(d);
                getUnread(u.getPeer()).add(u);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        for (Peer peer : unreadMessages.keySet()) {
            for (UnreadMessage u : unreadMessages.get(peer)) {
                writer.writeObject(1, u);
            }
        }
    }
}
