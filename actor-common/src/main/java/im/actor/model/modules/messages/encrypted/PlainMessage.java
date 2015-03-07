package im.actor.model.modules.messages.encrypted;

import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

/**
 * Created by ex3ndr on 08.03.15.
 */
public class PlainMessage extends BserObject {

    public static PlainMessage fromBytes(byte[] data) throws IOException {
        return Bser.parse(new PlainMessage(), data);
    }

    private long rid;
    private int messageType;
    private byte[] body;

    public PlainMessage(long rid, int messageType, byte[] body) {
        this.rid = rid;
        this.messageType = messageType;
        this.body = body;
    }

    private PlainMessage() {
        
    }

    public long getRid() {
        return rid;
    }

    public int getMessageType() {
        return messageType;
    }

    public byte[] getBody() {
        return body;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        rid = values.getLong(1);
        messageType = values.getInt(2);
        body = values.getBytes(3);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, rid);
        writer.writeInt(2, messageType);
        writer.writeBytes(3, body);
    }
}
