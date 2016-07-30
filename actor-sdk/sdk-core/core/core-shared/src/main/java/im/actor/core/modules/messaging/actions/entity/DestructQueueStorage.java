package im.actor.core.modules.messaging.actions.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class DestructQueueStorage extends BserObject {

    public static DestructQueueStorage fromBytes(byte[] data) {
        try {
            return Bser.parse(new DestructQueueStorage(), data);
        } catch (IOException e) {
            e.printStackTrace();
            return new DestructQueueStorage();
        }
    }

    private List<DestructQueueMessage> queue;

    public DestructQueueStorage(List<DestructQueueMessage> queue) {
        this.queue = queue;
    }

    public DestructQueueStorage() {
        this.queue = new ArrayList<>();
    }

    public List<DestructQueueMessage> getQueue() {
        return queue;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        for (byte[] b : values.getRepeatedBytes(1)) {
            queue.add(DestructQueueMessage.fromBytes(b));
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeRepeatedObj(1, queue);
    }
}
