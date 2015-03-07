package im.actor.model.modules.messages.encrypted;

import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

/**
 * Created by ex3ndr on 08.03.15.
 */
public class MessageContainer extends BserObject {

    public static MessageContainer fromBytes(byte[] data) throws IOException {
        return Bser.parse(new MessageContainer(), data);
    }

    private int messageType;
    private byte[] body;
    private long crc32;

    public MessageContainer(int messageType, byte[] body, long crc32) {
        this.messageType = messageType;
        this.body = body;
        this.crc32 = crc32;
    }

    private MessageContainer() {

    }

    public int getMessageType() {
        return messageType;
    }

    public byte[] getBody() {
        return body;
    }

    public long getCrc32() {
        return crc32;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.messageType = values.getInt(1);
        this.body = values.getBytes(2);
        this.crc32 = values.getLong(3);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, messageType);
        writer.writeBytes(2, body);
        writer.writeLong(3, crc32);
    }
}
