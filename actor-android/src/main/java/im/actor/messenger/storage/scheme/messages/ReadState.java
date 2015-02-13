package im.actor.messenger.storage.scheme.messages;

import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;
import com.droidkit.engine.keyvalue.KeyValueIdentity;

import java.io.IOException;

import im.actor.messenger.model.DialogUids;

/**
 * Created by ex3ndr on 24.10.14.
 */
public class ReadState extends BserObject implements KeyValueIdentity {

    private int chatType;
    private int chatId;

    private long lastReadMessage;
    private long lastReadSortingKey;

    public ReadState(int chatType, int chatId, long lastReadMessage, long lastReadSortingKey) {
        this.chatType = chatType;
        this.chatId = chatId;
        this.lastReadMessage = lastReadMessage;
        this.lastReadSortingKey = lastReadSortingKey;
    }

    public ReadState() {
    }

    public int getChatId() {
        return chatId;
    }

    public int getChatType() {
        return chatType;
    }

    public long getLastReadMessage() {
        return lastReadMessage;
    }

    public long getLastReadSortingKey() {
        return lastReadSortingKey;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        chatType = values.getInt(1);
        chatId = values.getInt(2);
        lastReadMessage = values.getLong(3, 0);
        lastReadSortingKey = values.getLong(4, 0);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, chatType);
        writer.writeInt(2, chatId);
        writer.writeLong(3, lastReadMessage);
        writer.writeLong(4, lastReadSortingKey);
    }

    @Override
    public long getKeyValueId() {
        return DialogUids.getDialogUid(chatType, chatId);
    }
}
