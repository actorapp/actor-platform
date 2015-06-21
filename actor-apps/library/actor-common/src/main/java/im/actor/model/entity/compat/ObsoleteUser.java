/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.compat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.model.api.ContactType;
import im.actor.model.api.Sex;
import im.actor.model.api.User;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

public class ObsoleteUser extends BserObject {

    private int uid;
    private long accessHash;
    @NotNull
    @SuppressWarnings("NullableProblems")
    private String name;
    @Nullable
    private String localName;
    @NotNull
    @SuppressWarnings("NullableProblems")
    private im.actor.model.api.Sex sex;
    @Nullable
    private ObsoleteAvatar avatar;
    @NotNull
    @SuppressWarnings("NullableProblems")
    private List<ObsoleteContactRecord> records;

    public ObsoleteUser(byte[] data) throws IOException {
        load(data);
    }

    public ObsoleteUser(BserValues values) throws IOException {
        parse(values);
    }

    public User toApiUser() {
        List<im.actor.model.api.ContactRecord> records = new ArrayList<im.actor.model.api.ContactRecord>();
        for (ObsoleteContactRecord r : this.records) {
            if (r.getRecordType() == 0) {
                if (r.getRecordData().equals("0")) {
                    continue;
                }
                records.add(new im.actor.model.api.ContactRecord(ContactType.PHONE, null,
                        Long.parseLong(r.getRecordData()), r.getRecordTitle(), null));
            }
        }
        return new im.actor.model.api.User(
                uid,
                accessHash,
                name,
                localName,
                sex,
                avatar != null ? avatar.toApiAvatar() : null,
                records,
                false/*Obsolete version doesn't contain bot flag*/);
    }

    public int getUid() {
        return uid;
    }

    public long getAccessHash() {
        return accessHash;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public String getLocalName() {
        return localName;
    }

    @NotNull
    public Sex getSex() {
        return sex;
    }

    @Nullable
    public ObsoleteAvatar getAvatar() {
        return avatar;
    }

    @NotNull
    public List<ObsoleteContactRecord> getRecords() {
        return records;
    }

    @Override
    public void parse(@NotNull BserValues values) throws IOException {
        uid = values.getInt(1);
        accessHash = values.getLong(2);
        name = values.getString(3);
        localName = values.optString(4);
        sex = im.actor.model.api.Sex.UNKNOWN;
        switch (values.getInt(6)) {
            case 3:
                sex = im.actor.model.api.Sex.FEMALE;
                break;
            case 2:
                sex = im.actor.model.api.Sex.MALE;
                break;
        }

        byte[] a = values.optBytes(5);
        if (a != null) {
            avatar = new ObsoleteAvatar(a);
        }

        int count = values.getRepeatedCount(7);
        records = new ArrayList<ObsoleteContactRecord>();
        if (count > 0) {
            List<byte[]> raw = values.getRepeatedBytes(7);
            for (int i = 0; i < count; i++) {
                records.add(new ObsoleteContactRecord(raw.get(i)));
            }
        }
    }

    @Override
    public void serialize(@NotNull BserWriter writer) throws IOException {
        throw new UnsupportedOperationException();
    }
}
