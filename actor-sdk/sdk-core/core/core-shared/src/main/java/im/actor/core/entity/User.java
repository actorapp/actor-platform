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
import im.actor.core.api.ApiBotCommand;
import im.actor.core.api.ApiContactRecord;
import im.actor.core.api.ApiContactType;
import im.actor.core.api.ApiFullUser;
import im.actor.core.api.ApiInt32Value;
import im.actor.core.api.ApiMapValue;
import im.actor.core.api.ApiMapValueItem;
import im.actor.core.api.ApiUser;
import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.storage.KeyValueItem;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class User extends WrapperExtEntity<ApiFullUser, ApiUser> implements KeyValueItem {

    private static final int RECORD_ID = 10;
    private static final int RECORD_FULL_ID = 20;

    public static BserCreator<User> CREATOR = User::new;

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
    @Property("readonly, nonatomic")
    private boolean isBlocked;
    @Nullable
    @Property("readonly, nonatomic")
    private String timeZone;
    @Property("readonly, nonatomic")
    private boolean isVerified;
    @Property("readonly, nonatomic")
    private List<BotCommand> commands;


    @NotNull
    @Property("readonly, nonatomic")
    private boolean haveExtension;

    public User(@NotNull ApiUser wrappedUser, @Nullable ApiFullUser ext) {
        super(RECORD_ID, RECORD_FULL_ID, wrappedUser, ext);
    }

    public User(@NotNull byte[] data) throws IOException {
        super(RECORD_ID, RECORD_FULL_ID, data);
    }

    private User() {
        super(RECORD_ID, RECORD_FULL_ID);
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

    public boolean isHaveExtension() {
        return haveExtension;
    }

    @NotNull
    public List<ContactRecord> getRecords() {
        return records;
    }

    public boolean isBot() {
        return isBot;
    }

    public List<BotCommand> getCommands() {
        return commands;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    @Nullable
    public String getTimeZone() {
        return timeZone;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public User editName(@NotNull String name) {
        ApiUser w = getWrapped();
        ApiUser res = new ApiUser(
                w.getId(),
                w.getAccessHash(),
                name,
                w.getLocalName(),
                w.getNick(),
                w.getSex(),
                w.getAvatar(),
                w.isBot(),
                w.getExt());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new User(res, getWrappedExt());
    }

    public User editLocalName(@NotNull String localName) {
        ApiUser w = getWrapped();
        ApiUser res = new ApiUser(
                w.getId(),
                w.getAccessHash(),
                w.getName(),
                localName,
                w.getNick(),
                w.getSex(),
                w.getAvatar(),
                w.isBot(),
                w.getExt());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new User(res, getWrappedExt());
    }

    public User editNick(@Nullable String nick) {
        ApiUser w = getWrapped();
        ApiUser res = new ApiUser(
                w.getId(),
                w.getAccessHash(),
                w.getName(),
                w.getLocalName(),
                nick,
                w.getSex(),
                w.getAvatar(),
                w.isBot(),
                w.getExt());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new User(res, getWrappedExt());
    }

    public User editExt(@Nullable ApiMapValue ext) {
        ApiUser w = getWrapped();
        ApiUser res = new ApiUser(
                w.getId(),
                w.getAccessHash(),
                w.getName(),
                w.getLocalName(),
                w.getNick(),
                w.getSex(),
                w.getAvatar(),
                w.isBot(),
                ext);
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new User(res, getWrappedExt());
    }

    public User editAvatar(@Nullable ApiAvatar avatar) {
        ApiUser w = getWrapped();
        ApiUser res = new ApiUser(
                w.getId(),
                w.getAccessHash(),
                w.getName(),
                w.getLocalName(),
                w.getNick(),
                w.getSex(),
                avatar,
                w.isBot(),
                w.getExt());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new User(res, getWrappedExt());
    }

    public User updateExt(@Nullable ApiFullUser ext) {
        return new User(getWrapped(), ext);
    }

    public User editAbout(@Nullable String about) {
        ApiFullUser ext = getWrappedExt();
        if (ext != null) {
            ApiFullUser upd = new ApiFullUser(
                    ext.getId(),
                    ext.getContactInfo(),
                    about,
                    ext.getPreferredLanguages(),
                    ext.getTimeZone(),
                    ext.getBotCommands(),
                    ext.getExt(),
                    ext.isBlocked()
            );
            return new User(getWrapped(), upd);
        } else {
            return this;
        }
    }

    public User editPreferredLanguages(List<String> preferredLanguages) {
        ApiFullUser ext = getWrappedExt();
        if (ext != null) {
            ApiFullUser upd = new ApiFullUser(
                    ext.getId(),
                    ext.getContactInfo(),
                    ext.getAbout(),
                    preferredLanguages,
                    ext.getTimeZone(),
                    ext.getBotCommands(),
                    ext.getExt(),
                    ext.isBlocked()
            );
            return new User(getWrapped(), upd);
        } else {
            return this;
        }
    }

    public User editTimeZone(String timeZone) {
        ApiFullUser ext = getWrappedExt();
        if (ext != null) {
            ApiFullUser upd = new ApiFullUser(
                    ext.getId(),
                    ext.getContactInfo(),
                    ext.getAbout(),
                    ext.getPreferredLanguages(),
                    timeZone,
                    ext.getBotCommands(),
                    ext.getExt(),
                    ext.isBlocked()
            );
            return new User(getWrapped(), upd);
        } else {
            return this;
        }
    }

    public User editContacts(List<ApiContactRecord> contacts) {
        ApiFullUser ext = getWrappedExt();
        if (ext != null) {
            ApiFullUser upd = new ApiFullUser(
                    ext.getId(),
                    contacts,
                    ext.getAbout(),
                    ext.getPreferredLanguages(),
                    ext.getTimeZone(),
                    ext.getBotCommands(),
                    ext.getExt(),
                    ext.isBlocked()
            );
            return new User(getWrapped(), upd);
        } else {
            return this;
        }
    }

    public User editBotCommands(List<ApiBotCommand> commands) {
        ApiFullUser ext = getWrappedExt();
        if (ext != null) {
            ApiFullUser upd = new ApiFullUser(
                    ext.getId(),
                    ext.getContactInfo(),
                    ext.getAbout(),
                    ext.getPreferredLanguages(),
                    ext.getTimeZone(),
                    commands,
                    ext.getExt(),
                    ext.isBlocked()
            );
            return new User(getWrapped(), upd);
        } else {
            return this;
        }
    }

    public User editFullExt(ApiMapValue extv) {
        ApiFullUser ext = getWrappedExt();
        if (ext != null) {
            ApiFullUser upd = new ApiFullUser(
                    ext.getId(),
                    ext.getContactInfo(),
                    ext.getAbout(),
                    ext.getPreferredLanguages(),
                    ext.getTimeZone(),
                    ext.getBotCommands(),
                    extv,
                    ext.isBlocked()
            );
            return new User(getWrapped(), upd);
        } else {
            return this;
        }
    }

    public User editBlocked(boolean isBlocked) {
        ApiFullUser ext = getWrappedExt();
        if (ext != null) {
            ApiFullUser upd = new ApiFullUser(
                    ext.getId(),
                    ext.getContactInfo(),
                    ext.getAbout(),
                    ext.getPreferredLanguages(),
                    ext.getTimeZone(),
                    ext.getBotCommands(),
                    ext.getExt(),
                    isBlocked
            );
            return new User(getWrapped(), upd);
        } else {
            return this;
        }
    }


    @Override
    protected void applyWrapped(@NotNull ApiUser wrapped, @Nullable ApiFullUser ext) {
        this.uid = wrapped.getId();
        this.accessHash = wrapped.getAccessHash();
        this.name = wrapped.getName();
        this.localName = wrapped.getLocalName();
        if (wrapped.getNick() != null && wrapped.getNick().length() > 0) {
            this.username = wrapped.getNick();
        } else {
            this.username = null;
        }
        if (wrapped.getAvatar() != null) {
            this.avatar = new Avatar(wrapped.getAvatar());
        }
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

        if (wrapped.getExt() != null) {
            this.isVerified = true;
            for (ApiMapValueItem i : wrapped.getExt().getItems()) {
                if ("is_verified".equals(i.getKey())) {
                    if (i.getValue() instanceof ApiInt32Value) {
                        this.isVerified = ((ApiInt32Value) i.getValue()).getValue() > 0;
                    }
                }
            }
        }

        // Extension

        if (ext != null) {
            this.haveExtension = true;
            this.records = new ArrayList<>();
            this.commands = new ArrayList<BotCommand>();

            if (ext.isBlocked() != null) {
                this.isBlocked = ext.isBlocked();
            } else {
                this.isBlocked = false;
            }
            this.timeZone = ext.getTimeZone();
            for (ApiContactRecord record : ext.getContactInfo()) {
                if (record.getType() == ApiContactType.PHONE) {
                    this.records.add(new ContactRecord(ContactRecordType.PHONE, record.getTypeSpec(), "" + record.getLongValue(),
                            record.getTitle(), record.getSubtitle()));
                } else if (record.getType() == ApiContactType.EMAIL) {
                    this.records.add(new ContactRecord(ContactRecordType.EMAIL, record.getTypeSpec(), record.getStringValue(),
                            record.getTitle(), record.getSubtitle()));
                } else if (record.getType() == ApiContactType.WEB) {
                    this.records.add(new ContactRecord(ContactRecordType.WEB, record.getTypeSpec(), record.getStringValue(),
                            record.getTitle(), record.getSubtitle()));
                } else if (record.getType() == ApiContactType.SOCIAL) {
                    this.records.add(new ContactRecord(ContactRecordType.SOCIAL, record.getTypeSpec(), record.getStringValue(),
                            record.getTitle(), record.getSubtitle()));
                }
            }

            //Bot commands
            for (ApiBotCommand command : ext.getBotCommands()) {
                commands.add(new BotCommand(command.getSlashCommand(), command.getDescription(), command.getLocKey()));
            }


            this.about = ext.getAbout();
        } else {
            this.isBlocked = false;
            this.haveExtension = false;
            this.records = new ArrayList<>();
            this.commands = new ArrayList<BotCommand>();
            this.about = null;
            this.timeZone = null;
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

    @Override
    protected ApiFullUser createExtInstance() {
        return new ApiFullUser();
    }
}