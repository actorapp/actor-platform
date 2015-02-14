package im.actor.messenger.storage.adapters;

import com.droidkit.engine.list.DataAdapter;
import im.actor.model.entity.Message;

import java.io.IOException;

/**
 * Created by ex3ndr on 14.02.15.
 */
public class MessagesAdapter implements DataAdapter<Message> {
    @Override
    public long getId(Message value) {
        return value.getListId();
    }

    @Override
    public long getSortKey(Message value) {
        return value.getListSortKey();
    }

    @Override
    public byte[] serialize(Message entity) {
        return entity.toByteArray();
    }

    @Override
    public Message deserialize(byte[] item) {
        try {
            return Message.fromBytes(item);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
