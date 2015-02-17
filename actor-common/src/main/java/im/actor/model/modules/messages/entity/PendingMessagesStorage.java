package im.actor.model.modules.messages.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

/**
 * Created by ex3ndr on 17.02.15.
 */
public class PendingMessagesStorage extends BserObject {

    public static PendingMessagesStorage fromBytes(byte[] data) throws IOException {
        return Bser.parse(new PendingMessagesStorage(), data);
    }

    private ArrayList<PendingMessage> messages = new ArrayList<PendingMessage>();

    public ArrayList<PendingMessage> getMessages() {
        return messages;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        messages.clear();
        int count = values.getRepeatedCount(1);
        List<PendingMessage> tmp = new ArrayList<PendingMessage>();
        for (int i = 0; i < count; i++) {
            tmp.add(new PendingMessage());
        }
        messages.addAll(values.getRepeatedObj(1, tmp));
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeRepeatedObj(1, messages);
    }
}
