/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.model.api.*;
import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.droidkit.engine.KeyValueItem;
import im.actor.model.modules.messages.entity.EntityConverter;

public class User extends WrapperEntity<im.actor.model.api.User> implements KeyValueItem {

    public static User fromBytes(byte[] data) throws IOException {
        return Bser.parse(new User(), data);
    }

    private static final int RECORD_ID = 10;

    private int uid;
    private long accessHash;
    private String name;
    private String localName;
    private Avatar avatar;
    private Sex sex;
    private List<ContactRecord> records;

    public User(im.actor.model.api.User wrappedUser) {
        super(RECORD_ID, wrappedUser);
    }

    private User() {
        super(RECORD_ID);
    }

    public Peer peer() {
        return new Peer(PeerType.PRIVATE, uid);
    }

    public int getUid() {
        return uid;
    }

    public long getAccessHash() {
        return accessHash;
    }

    public String getServerName() {
        return name;
    }

    public String getLocalName() {
        return localName;
    }

    public String getName() {
        if (localName == null) {
            return name;
        } else {
            return localName;
        }
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public Sex getSex() {
        return sex;
    }

    public List<ContactRecord> getRecords() {
        return records;
    }

    public User editName(String name) {
        im.actor.model.api.User w = getWrapped();
        User res = new User(new im.actor.model.api.User(
                w.getId(),
                w.getAccessHash(),
                name,
                w.getLocalName(),
                w.getSex(),
                w.getAvatar(),
                w.getContactInfo(),
                w.isBot()));
        res.setUnmappedObjects(w.getUnmappedObjects());
        return res;
    }

    public User editLocalName(String localName) {
        im.actor.model.api.User w = getWrapped();
        User res = new User(new im.actor.model.api.User(
                w.getId(),
                w.getAccessHash(),
                w.getName(),
                localName,
                w.getSex(),
                w.getAvatar(),
                w.getContactInfo(),
                w.isBot()));
        res.setUnmappedObjects(w.getUnmappedObjects());
        return res;
    }

    public User editAvatar(im.actor.model.api.Avatar avatar) {
        im.actor.model.api.User w = getWrapped();
        User res = new User(new im.actor.model.api.User(
                w.getId(),
                w.getAccessHash(),
                w.getName(),
                w.getLocalName(),
                w.getSex(),
                avatar,
                w.getContactInfo(),
                w.isBot()));
        res.setUnmappedObjects(w.getUnmappedObjects());
        return res;
    }

    @Override
    protected void applyWrapped(im.actor.model.api.User wrapped) {
        this.uid = wrapped.getId();
        this.accessHash = wrapped.getAccessHash();
        this.name = wrapped.getName();
        this.localName = wrapped.getLocalName();
        this.sex = EntityConverter.convert(wrapped.getSex());

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
        } else {
            this.avatar = new Avatar();
        }
    }

    @Override
    public void parse(BserValues values) throws IOException {
        // Is Old layout
        if (!values.getBool(8, false)) {
            int uid = values.getInt(1);
            long accessHash = values.getLong(2);
            String name = values.getString(3);
            String localName = values.optString(4);
            im.actor.model.api.Sex sex = im.actor.model.api.Sex.UNKNOWN;
            switch (Sex.fromValue(values.getInt(6))) {
                case FEMALE:
                    sex = im.actor.model.api.Sex.FEMALE;
                    break;
                case MALE:
                    sex = im.actor.model.api.Sex.MALE;
                    break;
            }

            im.actor.model.api.Avatar avatar = new im.actor.model.api.Avatar();
            byte[] a = values.optBytes(5);
            if (a != null) {
                avatar = Avatar.fromBytes(a).toWrapped();
            }

            List<im.actor.model.api.ContactRecord> records = new ArrayList<im.actor.model.api.ContactRecord>();
            int count = values.getRepeatedCount(7);
            if (count > 0) {
                List<ObsoleteContactRecord> rec = new ArrayList<ObsoleteContactRecord>();
                for (int i = 0; i < count; i++) {
                    rec.add(new ObsoleteContactRecord());
                }
                rec = values.getRepeatedObj(7, rec);
                for (ObsoleteContactRecord o : rec) {
                    if (o.getRecordType() == 0) {
                        records.add(new im.actor.model.api.ContactRecord(ContactType.PHONE, null,
                                Long.parseLong(o.getRecordData()), o.getRecordTitle(), null));
                    }
                }
            }

            setWrapped(new im.actor.model.api.User(uid, accessHash, name, localName, sex, avatar,
                    records, false));
        }

        super.parse(values);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        // Mark as new layout
        writer.writeBool(8, true);
        // Serialize wrapped object
        super.serialize(writer);
    }

    @Override
    public long getEngineId() {
        return getUid();
    }

    @Override
    protected im.actor.model.api.User createInstance() {
        return new im.actor.model.api.User();
    }

    private class ObsoleteContactRecord extends BserObject {

        private int id;
        private long accessHash;
        private int recordType;
        private String recordData;
        private String recordTitle;

        public int getId() {
            return id;
        }

        public long getAccessHash() {
            return accessHash;
        }

        public int getRecordType() {
            return recordType;
        }

        public String getRecordData() {
            return recordData;
        }

        public String getRecordTitle() {
            return recordTitle;
        }

        @Override
        public void parse(BserValues values) throws IOException {
            id = values.getInt(1);
            accessHash = values.getLong(2);
            recordType = values.getInt(3);
            recordData = values.getString(4);
            recordTitle = values.getString(5);
        }

        @Override
        public void serialize(BserWriter writer) throws IOException {
            writer.writeInt(1, id);
            writer.writeLong(2, accessHash);
            writer.writeInt(3, recordType);
            writer.writeString(4, recordData);
            writer.writeString(5, recordTitle);
        }
    }
}