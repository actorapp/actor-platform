package im.actor.messenger.storage.scheme;

import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;
import com.droidkit.engine.list.ListItemSearchIdentity;

import im.actor.messenger.storage.scheme.avatar.Avatar;

import java.io.IOException;

/**
 * Created by ex3ndr on 18.10.14.
 */
public class Contact extends BserObject implements ListItemSearchIdentity {
    private int uid;
    private long sortKey;
    private String name;
    private Avatar avatar;

    public Contact(Contact src, Avatar avatar) {
        this.sortKey = src.sortKey;
        this.uid = src.uid;
        this.name = src.getName();
        this.avatar = avatar;
    }

    public Contact(int uid, long sortKey, String name, Avatar avatar) {
        this.sortKey = sortKey;
        this.uid = uid;
        this.name = name;
        this.avatar = avatar;
    }

    public Contact() {

    }

    public long getSortKey() {
        return sortKey;
    }

    public int getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        sortKey = values.getLong(2);
        uid = values.getInt(4);
        name = values.getString(5);
        avatar = values.optObj(6, Avatar.class);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(2, sortKey);
        writer.writeInt(4, uid);
        writer.writeString(5, name);
        if (avatar != null) {
            writer.writeObject(6, avatar);
        }
    }

    @Override
    public String getQuery() {
        return name;
    }

    @Override
    public long getListId() {
        return uid;
    }

    @Override
    public long getListSortKey() {
        return sortKey;
    }
}
