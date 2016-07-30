package im.actor.core.modules.messaging.actions.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class DestructPendingStorage extends BserObject {

    public static DestructPendingStorage fromBytes(byte[] data) throws IOException {
        return Bser.parse(new DestructPendingStorage(), data);
    }

    private List<DestructPendingMessage> individualMessages;
    private List<DestructPendingMessage> messages;

    public DestructPendingStorage(List<DestructPendingMessage> individualMessages, List<DestructPendingMessage> messages) {
        this.individualMessages = individualMessages;
        this.messages = messages;
    }

    public DestructPendingStorage() {
        this.individualMessages = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    public List<DestructPendingMessage> getIndividualMessages() {
        return individualMessages;
    }

    public List<DestructPendingMessage> getMessages() {
        return messages;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        individualMessages.clear();
        for (byte[] b : values.getRepeatedBytes(1)) {
            individualMessages.add(DestructPendingMessage.fromBytes(b));
        }
        messages.clear();
        for (byte[] b : values.getRepeatedBytes(2)) {
            messages.add(DestructPendingMessage.fromBytes(b));
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeRepeatedObj(1, individualMessages);
        writer.writeRepeatedObj(2, messages);
    }
}
