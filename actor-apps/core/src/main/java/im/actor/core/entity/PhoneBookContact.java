/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import java.util.ArrayList;

public class PhoneBookContact {
    @Property("readonly, nonatomic")
    private long contactId;
    @Property("readonly, nonatomic")
    private String name;

    @Property("readonly, nonatomic")
    private ArrayList<PhoneBookPhone> phones = new ArrayList<PhoneBookPhone>();
    @Property("readonly, nonatomic")
    private ArrayList<PhoneBookEmail> emails = new ArrayList<PhoneBookEmail>();

    public PhoneBookContact(long contactId, String name, ArrayList<PhoneBookPhone> phones, ArrayList<PhoneBookEmail> emails) {
        this.contactId = contactId;
        this.name = name;
        this.phones = phones;
        this.emails = emails;
    }

    public ArrayList<PhoneBookPhone> getPhones() {
        return phones;
    }

    public ArrayList<PhoneBookEmail> getEmails() {
        return emails;
    }

    public String getName() {
        return name;
    }

    public long getContactId() {
        return contactId;
    }
}
