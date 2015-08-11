/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.messages.entity;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.core.entity.Peer;

public class PlainCursorsStorage extends BserObject {

    public static PlainCursorsStorage fromBytes(byte[] data) throws IOException {
        return Bser.parse(new PlainCursorsStorage(), data);
    }

    private HashMap<Peer, PlainCursor> cursors = new HashMap<Peer, PlainCursor>();

    public PlainCursor getCursor(Peer peer) {
        if (!cursors.containsKey(peer)) {
            cursors.put(peer, new PlainCursor(peer, 0, 0));
        }
        return cursors.get(peer);
    }

    public void putCursor(PlainCursor cursor) {
        cursors.put(cursor.getPeer(), cursor);
    }

    public Collection<PlainCursor> getAllCursors() {
        return cursors.values();
    }

    @Override
    public void parse(BserValues values) throws IOException {
        for (byte[] data : values.getRepeatedBytes(1)) {
            try {
                PlainCursor plainCursor = PlainCursor.fromBytes(data);
                cursors.put(plainCursor.getPeer(), plainCursor);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        for (PlainCursor cursor : cursors.values()) {
            writer.writeObject(1, cursor);
        }
    }
}
