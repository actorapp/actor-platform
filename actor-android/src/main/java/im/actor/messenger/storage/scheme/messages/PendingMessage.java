package im.actor.messenger.storage.scheme.messages;

import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 16.11.14.
 */
public class PendingMessage extends BserObject {

    private long rid;
    private int convType;
    private int convId;
    private byte[] messageContent;
    private int messageType;
    private boolean isEncrypted;

    public PendingMessage(long rid, int convType, int convId, byte[] messageContent, int messageType, boolean isEncrypted) {
        this.rid = rid;
        this.convType = convType;
        this.convId = convId;
        this.messageContent = messageContent;
        this.messageType = messageType;
        this.isEncrypted = isEncrypted;
    }

    public PendingMessage() {
    }

    public long getRid() {
        return rid;
    }

    public int getConvType() {
        return convType;
    }

    public int getConvId() {
        return convId;
    }

    public byte[] getMessageContent() {
        return messageContent;
    }

    public int getMessageType() {
        return messageType;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        rid = values.getLong(1);
        convType = values.getInt(2);
        convId = values.getInt(3);
        messageContent = values.getBytes(4);
        messageType = values.getInt(5, 0);
        isEncrypted = values.getBool(6, true);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, rid);
        writer.writeInt(2, convType);
        writer.writeInt(3, convId);
        writer.writeBytes(4, messageContent);
        writer.writeInt(5, messageType);
        writer.writeBool(6, isEncrypted);
    }
}
