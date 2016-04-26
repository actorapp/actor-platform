/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiTextExMarkdown;
import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.storage.ListEngineItem;

public class PhoneBookContact extends BserObject implements ListEngineItem {
    @Property("readonly, nonatomic")
    private long contactId;
    @Property("readonly, nonatomic")
    private long sortId;
    @Property("readonly, nonatomic")
    private String name;

    @Property("readonly, nonatomic")
    private List<PhoneBookPhone> phones = new ArrayList<PhoneBookPhone>();
    @Property("readonly, nonatomic")
    private List<PhoneBookEmail> emails = new ArrayList<PhoneBookEmail>();

    public static final String ENTITY_NAME = "PhoneBookContact";


    public static final BserCreator<PhoneBookContact> CREATOR = new BserCreator<PhoneBookContact>() {
        @Override
        public PhoneBookContact createInstance() {
            return new PhoneBookContact();
        }
    };


    public PhoneBookContact(long contactId, String name, List<PhoneBookPhone> phones, List<PhoneBookEmail> emails) {
        this(contactId, name, phones, emails, contactId);
    }

    public PhoneBookContact(long contactId, String name, List<PhoneBookPhone> phones, List<PhoneBookEmail> emails, long sortId) {
        this.contactId = contactId;
        this.name = name;
        this.phones = phones;
        this.emails = emails;
        this.sortId = sortId;
    }

    public PhoneBookContact() {

    }

    public List<PhoneBookPhone> getPhones() {
        return phones;
    }

    public List<PhoneBookEmail> getEmails() {
        return emails;
    }

    public String getName() {
        return name;
    }

    public long getContactId() {
        return contactId;
    }

    public PhoneBookContact setSortId(long sortId) {
        this.sortId = sortId;
        return this;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        contactId = values.getLong(1);
        name = values.getString(2);
        for (byte[] ph : values.getRepeatedBytes(3)) {
            phones.add(Bser.parse(new PhoneBookPhone(), ph));
        }

        for (byte[] em : values.getRepeatedBytes(4)) {
            emails.add(Bser.parse(new PhoneBookEmail(), em));
        }
        sortId = values.getLong(5, 0);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, contactId);
        writer.writeString(2, name);
        for (PhoneBookPhone ph : phones) {
            writer.writeObject(3, ph);
        }
        for (PhoneBookEmail em : emails) {
            writer.writeObject(4, em);
        }
        writer.writeLong(5, sortId);
    }

    @Override
    public long getEngineId() {
        return contactId;
    }

    @Override
    public long getEngineSort() {
        return sortId;
    }

    @Override
    public String getEngineSearch() {
        return name;
    }
}
