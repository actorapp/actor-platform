package im.actor.messenger.storage.adapters;

import com.droidkit.engine.list.DataAdapter;
import im.actor.model.entity.Dialog;

import java.io.IOException;

/**
 * Created by ex3ndr on 14.02.15.
 */
public class DialogsAdapter implements DataAdapter<Dialog> {
    @Override
    public long getId(Dialog value) {
        return value.getListId();
    }

    @Override
    public long getSortKey(Dialog value) {
        return value.getListSortKey();
    }

    @Override
    public byte[] serialize(Dialog entity) {
        return entity.toByteArray();
    }

    @Override
    public Dialog deserialize(byte[] item) {
        try {
            return Dialog.fromBytes(item);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
