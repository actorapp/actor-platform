/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.model.api.ContactType;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.droidkit.engine.KeyValueItem;
import im.actor.model.entity.compat.ObsoleteUser;

public class User extends WrapperEntity<im.actor.model.api.User> implements KeyValueItem {

    private static final int RECORD_ID = 10;

    private int uid;
    private long accessHash;
    @NotNull
    @SuppressWarnings("NullableProblems")
    private String name;
    @Nullable
    private String localName;
    @Nullable
    private Avatar avatar;
    @NotNull
    @SuppressWarnings("NullableProblems")
    private Sex sex;
    private boolean isBot;
    @NotNull
    @SuppressWarnings("NullableProblems")
    private List<ContactRecord> records;

    public User(@NotNull im.actor.model.api.User wrappedUser) {
        super(RECORD_ID, wrappedUser);
    }

    public User(@NotNull byte[] data) throws IOException {
        super(RECORD_ID, data);
    }

    @NotNull
    public Peer peer() {
        return new Peer(PeerType.PRIVATE, uid);
    }

    public int getUid() {
        return uid;
    }

    public long getAccessHash() {
        return accessHash;
    }

    @NotNull
    public String getServerName() {
        return name;
    }

    @Nullable
    public String getLocalName() {
        return localName;
    }

    @NotNull
    public String getName() {
        if (localName == null) {
            return name;
        } else {
            return localName;
        }
    }

    @Nullable
    public Avatar getAvatar() {
        return avatar;
    }

    @NotNull
    public Sex getSex() {
        return sex;
    }

    @NotNull
    public List<ContactRecord> getRecords() {
        return records;
    }

    public boolean isBot() {
        return isBot;
    }

    public User editName(@NotNull String name) {
        im.actor.model.api.User w = getWrapped();
        im.actor.model.api.User res = new im.actor.model.api.User(
                w.getId(),
                w.getAccessHash(),
                name,
                w.getLocalName(),
                w.getSex(),
                w.getAvatar(),
                w.getContactInfo(),
                w.isBot());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new User(res);
    }

    public User editLocalName(@NotNull String localName) {
        im.actor.model.api.User w = getWrapped();
        im.actor.model.api.User res = new im.actor.model.api.User(
                w.getId(),
                w.getAccessHash(),
                w.getName(),
                localName,
                w.getSex(),
                w.getAvatar(),
                w.getContactInfo(),
                w.isBot());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new User(res);
    }

    public User editAvatar(@Nullable im.actor.model.api.Avatar avatar) {
        im.actor.model.api.User w = getWrapped();
        im.actor.model.api.User res = new im.actor.model.api.User(
                w.getId(),
                w.getAccessHash(),
                w.getName(),
                w.getLocalName(),
                w.getSex(),
                avatar,
                w.getContactInfo(),
                w.isBot());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new User(res);
    }

    @Override
    protected void applyWrapped(@NotNull im.actor.model.api.User wrapped) {
        this.uid = wrapped.getId();
        this.accessHash = wrapped.getAccessHash();
        this.name = wrapped.getName();
        this.localName = wrapped.getLocalName();
        this.isBot = false;
        if (wrapped.isBot() != null) {
            this.isBot = wrapped.isBot();
        }
        this.sex = Sex.UNKNOWN;
        if (wrapped.getSex() != null) {
            switch (wrapped.getSex()) {
                case FEMALE:
                    this.sex = Sex.FEMALE;
                    break;
                case MALE:
                    this.sex = Sex.MALE;
                    break;
            }
        }

        this.records = new ArrayList<ContactRecord>();
        for (im.actor.model.api.ContactRecord record : wrapped.getContactInfo()) {
            if (record.getType() == ContactType.PHONE) {
                this.records.add(new ContactRecord(ContactRecordType.PHONE, "" + record.getLongValue(),
                        record.getTitle()));
            } else if (record.getType() == ContactType.EMAIL) {
                this.records.add(new ContactRecord(ContactRecordType.EMAIL, record.getStringValue(),
                        record.getTitle()));
            }
        }

        if (wrapped.getAvatar() != null) {
            this.avatar = new Avatar(wrapped.getAvatar());
        }
    }

    @Override
    public void parse(BserValues values) throws IOException {
        // Is Wrapper Layout
        if (values.getBool(8, false)) {
            // Parse wrapper layout
            super.parse(values);
        } else {
            // Convert old layout
            setWrapped(new ObsoleteUser(values).toApiUser());
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        // Mark as wrapper layout
        writer.writeBool(8, true);
        // Serialize wrapper layout
        super.serialize(writer);
    }

    @Override
    public long getEngineId() {
        return getUid();
    }

    @Override
    @NotNull
    protected im.actor.model.api.User createInstance() {
        return new im.actor.model.api.User();
    }

}