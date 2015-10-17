package im.actor.core.viewmodel.generics;

import java.util.ArrayList;
import java.util.Collection;

import im.actor.core.entity.ContactRecord;

public class ArrayListContactRecord extends ArrayList<ContactRecord> {

    public ArrayListContactRecord(int capacity) {
        super(capacity);
    }

    public ArrayListContactRecord() {
    }

    public ArrayListContactRecord(Collection<? extends ContactRecord> collection) {
        super(collection);
    }

    @Override
    public ContactRecord get(int index) {
        return super.get(index);
    }
}
