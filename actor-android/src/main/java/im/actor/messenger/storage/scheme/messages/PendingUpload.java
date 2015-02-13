package im.actor.messenger.storage.scheme.messages;

import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 25.10.14.
 */
public class PendingUpload extends BserObject {
    private int chatType;
    private int chatId;
    private long rid;
    private String fileName;
    private String name;
    private int messageType;
    private byte[] metadata;
    private byte[] thumb;
    private boolean isStopped;
    private boolean isEncrypted;

    public PendingUpload(int chatType, int chatId, long rid, String fileName, String name, int messageType, byte[] metadata, byte[] thumb,
                         boolean isStopped, boolean isEncrypted) {
        this.chatType = chatType;
        this.chatId = chatId;
        this.rid = rid;
        this.fileName = fileName;
        this.name = name;
        this.messageType = messageType;
        this.metadata = metadata;
        this.thumb = thumb;
        this.isStopped = isStopped;
        this.isEncrypted = isEncrypted;
    }

    public PendingUpload() {
    }

    public int getChatType() {
        return chatType;
    }

    public int getChatId() {
        return chatId;
    }

    public long getRid() {
        return rid;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getMetadata() {
        return metadata;
    }

    public String getName() {
        return name;
    }

    public int getMessageType() {
        return messageType;
    }

    public byte[] getThumb() {
        return thumb;
    }

    public boolean isStopped() {
        return isStopped;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public PendingUpload start() {
        return new PendingUpload(chatType, chatId, rid, fileName, name, messageType, metadata, thumb, false, isEncrypted);
    }

    public PendingUpload stop() {
        return new PendingUpload(chatType, chatId, rid, fileName, name, messageType, metadata, thumb, true, isEncrypted);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        chatType = values.getInt(1);
        chatId = values.getInt(2);
        rid = values.getLong(3);
        fileName = values.getString(4);
        name = values.getString(5);
        messageType = values.getInt(6);
        metadata = values.getBytes(7);
        thumb = values.optBytes(8);
        isStopped = values.optBool(9);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, chatType);
        writer.writeInt(2, chatId);
        writer.writeLong(3, rid);
        writer.writeString(4, fileName);
        writer.writeString(5, name);
        writer.writeInt(6, messageType);
        writer.writeBytes(7, metadata);
        if (thumb != null) {
            writer.writeBytes(8, thumb);
        }
        writer.writeBool(9, isStopped);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PendingUpload that = (PendingUpload) o;

        if (chatId != that.chatId) return false;
        if (chatType != that.chatType) return false;
        if (rid != that.rid) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = chatType;
        result = 31 * result + chatId;
        result = 31 * result + (int) (rid ^ (rid >>> 32));
        return result;
    }
}
