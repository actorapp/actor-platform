package im.actor.core.modules.encryption.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class UserKeys extends BserObject {

    private int uid;
    private UserKeysGroup[] userKeysGroups;
    private int[] invalidGroups;

    public UserKeys(int uid, UserKeysGroup[] userKeysGroups, int[] invalidGroups) {
        this.uid = uid;
        this.userKeysGroups = userKeysGroups;
        this.invalidGroups = invalidGroups;
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

    public int[] getInvalidGroups() {
        return invalidGroups;
    }

    public UserKeys addUserKeyGroup(UserKeysGroup keysGroup) {
        ArrayList<UserKeysGroup> userKeysGroups = new ArrayList<UserKeysGroup>();
        for (UserKeysGroup g : this.userKeysGroups) {
            userKeysGroups.add(g);
        }
        userKeysGroups.add(keysGroup);
        return new UserKeys(uid, userKeysGroups.toArray(new UserKeysGroup[userKeysGroups.size()]), invalidGroups);
    }

    public UserKeys removeUserKeyGroup(int keyGroupId) {
        ArrayList<UserKeysGroup> userKeysGroups = new ArrayList<UserKeysGroup>();
        for (UserKeysGroup g : this.userKeysGroups) {
            if (g.getKeyGroupId() != keyGroupId) {
                userKeysGroups.add(g);
            }
        }
        return new UserKeys(uid, userKeysGroups.toArray(new UserKeysGroup[userKeysGroups.size()]), invalidGroups);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        uid = values.getInt(1);
        List<byte[]> g = values.getRepeatedBytes(2);
        userKeysGroups = new UserKeysGroup[g.size()];
        for (int i = 0; i < userKeysGroups.length; i++) {
            userKeysGroups[i] = new UserKeysGroup(g.get(i));
        }
        List<Integer> g2 = values.getRepeatedInt(3);
        invalidGroups = new int[g2.size()];
        for (int i = 0; i < invalidGroups.length; i++) {
            invalidGroups[i] = g2.get(i);
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, uid);
        for (UserKeysGroup ukg : userKeysGroups) {
            writer.writeObject(2, ukg);
        }
        for (int id : invalidGroups) {
            writer.writeInt(3, id);
        }
    }
}
