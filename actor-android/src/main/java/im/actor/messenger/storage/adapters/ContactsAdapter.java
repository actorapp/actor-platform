package im.actor.messenger.storage.adapters;

import com.droidkit.engine.list.FilterableDataAdapter;

import java.io.IOException;

import im.actor.model.entity.Contact;

/**
 * Created by ex3ndr on 25.02.15.
 */
public class ContactsAdapter implements FilterableDataAdapter<Contact> {
    @Override
    public long getId(Contact value) {
        return value.getListId();
    }

    @Override
    public long getSortKey(Contact value) {
        return value.getListSortKey();
    }

    @Override
    public byte[] serialize(Contact entity) {
        return entity.toByteArray();
    }

    @Override
    public Contact deserialize(byte[] item) {
        try {
            return Contact.fromBytes(item);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getFilterValue(Contact value) {
        return value.getName();
    }
}
