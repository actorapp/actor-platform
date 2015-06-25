/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.messages.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

public class OutUnreadMessagesStorage extends BserObject {

    public static OutUnreadMessagesStorage fromBytes(byte[] data) throws IOException {
        return Bser.parse(new OutUnreadMessagesStorage(), data);
    }

    private ArrayList<OutUnreadMessage> messages = new ArrayList<OutUnreadMessage>();

    public ArrayList<OutUnreadMessage> getMessages() {
        return messages;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        messages.clear();
        int count = values.getRepeatedCount(1);
        List<OutUnreadMessage> tmp = new ArrayList<OutUnreadMessage>();
        for (int i = 0; i < count; i++) {
            tmp.add(new OutUnreadMessage());
        }
        messages.addAll(values.getRepeatedObj(1, tmp));
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeRepeatedObj(1, messages);
    }
}
