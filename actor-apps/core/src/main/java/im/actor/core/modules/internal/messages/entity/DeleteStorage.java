/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.messages.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.core.entity.Peer;

public class DeleteStorage extends BserObject {

    public static DeleteStorage fromBytes(byte[] data) throws IOException {
        return Bser.parse(new DeleteStorage(), data);
    }

    private HashMap<Peer, Delete> pendingDeletions = new HashMap<Peer, Delete>();

    public HashMap<Peer, Delete> getPendingDeletions() {
        return pendingDeletions;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        pendingDeletions.clear();
        int count = values.getRepeatedCount(1);
        List<Delete> tmp = new ArrayList<Delete>();
        for (int i = 0; i < count; i++) {
            tmp.add(new Delete());
        }
        for (Delete d : values.getRepeatedObj(1, tmp)) {
            pendingDeletions.put(d.getPeer(), d);
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeRepeatedObj(1, new ArrayList<BserObject>(pendingDeletions.values()));
    }
}
