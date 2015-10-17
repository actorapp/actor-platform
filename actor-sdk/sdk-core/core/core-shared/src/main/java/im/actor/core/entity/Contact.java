/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.storage.ListEngineItem;

public class Contact extends BserObject implements ListEngineItem {

    public static final BserCreator<Contact> CREATOR = new BserCreator<Contact>() {
        @Override
        public Contact createInstance() {
            return new Contact();
        }
    };

    public static final String ENTITY_NAME = "Contact";

    @Property("readonly, nonatomic")
    private int uid;
    private long sortKey;
    @Nullable
    @Property("readonly, nonatomic")
    private Avatar avatar;
    @SuppressWarnings("NullableProblems")
    @NotNull
    @Property("readonly, nonatomic")
    private String name;

    public Contact(int uid, long sortKey, @Nullable Avatar avatar, @NotNull String name) {
        this.uid = uid;
        this.sortKey = sortKey;
        this.avatar = avatar;
        this.name = name;
    }

    private Contact() {

    }

    public int getUid() {
        return uid;
    }

    @Nullable
    public Avatar getAvatar() {
        return avatar;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        uid = values.getInt(1);
        sortKey = values.getLong(2);
        name = values.getString(3);
        if (values.optBytes(4) != null) {
            avatar = new Avatar(values.getBytes(4));
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, uid);
        writer.writeLong(2, sortKey);
        writer.writeString(3, name);
        if (avatar != null) {
            writer.writeObject(4, avatar);
        }
    }

    @Override
    public long getEngineId() {
        return uid;
    }

    @Override
    public long getEngineSort() {
        return sortKey;
    }

    @Override
    public String getEngineSearch() {
        return name;
    }
}
