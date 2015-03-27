package im.actor.model.entity;

import java.util.ArrayList;

/**
 * Created by ex3ndr on 25.02.15.
 */
public class PhoneBookContact {
    private long contactId;
    private String name;

    private ArrayList<PhoneBookPhone> phones = new ArrayList<PhoneBookPhone>();
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
