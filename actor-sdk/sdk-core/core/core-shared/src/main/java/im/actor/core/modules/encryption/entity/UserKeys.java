package im.actor.core.modules.encryption.entity;

import java.io.IOException;
import java.util.List;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class UserKeys extends BserObject {

    private int uid;
    private UserKeysGroup[] userKeysGroups;

    public UserKeys(int uid, UserKeysGroup[] userKeysGroups) {
        this.uid = uid;
        this.userKeysGroups = userKeysGroups;
    }

    public UserKeys(byte[] data) throws IOException {
        load(data);
    }

    public int getUid() {
        return uid;
    }

    public UserKeysGroup[] getUserKeysGroups() {
        return userKeysGroups;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        uid = values.getInt(1);
        List<byte[]> g = values.getRepeatedBytes(2);
        userKeysGroups = new UserKeysGroup[g.size()];
        for (int i = 0; i < userKeysGroups.length; i++) {
            userKeysGroups[i] = new UserKeysGroup(g.get(i));
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, uid);
        for (UserKeysGroup ukg : userKeysGroups) {
            writer.writeBytes(2, ukg.toByteArray());
        }
    }
}
