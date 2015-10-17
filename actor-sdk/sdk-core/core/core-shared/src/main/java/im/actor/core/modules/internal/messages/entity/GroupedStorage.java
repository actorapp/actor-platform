package im.actor.core.modules.internal.messages.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.entity.Peer;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class GroupedStorage extends BserObject {

    private ArrayList<GroupedItem> groups = new ArrayList<GroupedItem>();

    public GroupedStorage() {
    }

    public GroupedStorage(byte[] data) throws IOException {
        super.load(data);
    }

    public ArrayList<GroupedItem> getGroups() {
        return groups;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        for (byte[] data : values.getRepeatedBytes(1)) {
            groups.add(new GroupedItem(data));
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        for (GroupedItem g : groups) {
            writer.writeObject(1, g);
        }
    }
}
