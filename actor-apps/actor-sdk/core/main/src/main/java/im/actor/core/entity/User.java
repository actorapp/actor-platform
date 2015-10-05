/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiAvatar;
import im.actor.core.api.ApiContactRecord;
import im.actor.core.api.ApiContactType;
import im.actor.core.api.ApiUser;
import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.storage.KeyValueItem;

public class User extends WrapperEntity<ApiUser> implements KeyValueItem {

    private static final int RECORD_ID = 10;

    public static BserCreator<User> CREATOR = new BserCreator<User>() {
        @Override
        public User createInstance() {
            return new User();
        }
    };

    @Property("readonly, nonatomic")
    private int uid;
    @Property("readonly, nonatomic")
    private long accessHash;
    @NotNull
    @Property("readonly, nonatomic")
    @SuppressWarnings("NullableProblems")
    private String name;
    @Nullable
    @Property("readonly, nonatomic")
    private String localName;
    @Nullable
    @Property("readonly, nonatomic")
    private String username;
    @Nullable
    @Property("readonly, nonatomic")
    private String about;
    @Nullable
    @Property("readonly, nonatomic")
    private Avatar avatar;
    @NotNull
    @Property("readonly, nonatomic")
    @SuppressWarnings("NullableProblems")
    private Sex sex;
    @Property("readonly, nonatomic")
    private boolean isBot;
    @NotNull
    @Property("readonly, nonatomic")
    @SuppressWarnings("NullableProblems")
    private List<ContactRecord> records;

    public User(@NotNull ApiUser wrappedUser) {
        super(RECORD_ID, wrappedUser);
    }

    public User(@NotNull byte[] data) throws IOException {
        super(RECORD_ID, data);
    }

    private User() {
        super(RECORD_ID);
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
    public String getNick() {
        return username;
    }

    @Nullable
    public String getAbout() {
        return about;
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
        ApiUser w = getWrapped();
        ApiUser res = new ApiUser(
                w.getId(),
                w.getAccessHash(),
                name,
                w.getLocalName(),
                w.getSex(),
                w.getAvatar(),
                w.getContactInfo(),
                w.isBot(),
                w.getNick(),
                w.getAbout(),
                w.getExternal());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new User(res);
    }

    public User editLocalName(@NotNull String localName) {
        ApiUser w = getWrapped();
        ApiUser res = new ApiUser(
                w.getId(),
                w.getAccessHash(),
                w.getName(),
                localName,
                w.getSex(),
                w.getAvatar(),
                w.getContactInfo(),
                w.isBot(),
                w.getNick(),
                w.getAbout(),
                w.getExternal());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new User(res);
    }

    public User editNick(@Nullable String nick) {
        ApiUser w = getWrapped();
        ApiUser res = new ApiUser(
                w.getId(),
                w.getAccessHash(),
                w.getName(),
                w.getLocalName(),
                w.getSex(),
                w.getAvatar(),
                w.getContactInfo(),
                w.isBot(),
                nick,
                w.getAbout(),
                w.getExternal());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new User(res);
    }

    public User editAbout(@Nullable String about) {
        ApiUser w = getWrapped();
        ApiUser res = new ApiUser(
                w.getId(),
                w.getAccessHash(),
                w.getName(),
                w.getLocalName(),
                w.getSex(),
                w.getAvatar(),
                w.getContactInfo(),
                w.isBot(),
                w.getNick(),
                about,
                w.getExternal());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new User(res);
    }

    public User editAvatar(@Nullable ApiAvatar avatar) {
        ApiUser w = getWrapped();
        ApiUser res = new ApiUser(
                w.getId(),
                w.getAccessHash(),
                w.getName(),
                w.getLocalName(),
                w.getSex(),
                avatar,
                w.getContactInfo(),
                w.isBot(),
                w.getNick(),
                w.getAbout(),
                w.getExternal());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new User(res);
    }

    @Override
    protected void applyWrapped(@NotNull ApiUser wrapped) {
        this.uid = wrapped.getId();
        this.accessHash = wrapped.getAccessHash();
        this.name = wrapped.getName();
        this.localName = wrapped.getLocalName();
        if (wrapped.getNick() != null && wrapped.getNick().length() > 0) {
            this.username = wrapped.getNick();
        } else {
            this.username = null;
        }
        this.about = wrapped.getAbout();
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
        for (ApiContactRecord record : wrapped.getContactInfo()) {
            if (record.getType() == ApiContactType.PHONE) {
                this.records.add(new ContactRecord(ContactRecordType.PHONE, "" + record.getLongValue(),
                        record.getTitle()));
            } else if (record.getType() == ApiContactType.EMAIL) {
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
            throw new IOException("Unsupported obsolete format");
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
    protected ApiUser createInstance() {
        return new ApiUser();
    }

}