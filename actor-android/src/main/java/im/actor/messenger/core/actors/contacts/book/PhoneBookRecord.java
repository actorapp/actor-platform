package im.actor.messenger.core.actors.contacts.book;

import java.util.ArrayList;

/**
 * Created by ex3ndr on 11.09.14.
 */
public class PhoneBookRecord {
    private long contactId;
    private String name;
    private ArrayList<PhoneBookPhone> phones = new ArrayList<PhoneBookPhone>();
    private ArrayList<PhoneBookEmail> emails = new ArrayList<PhoneBookEmail>();

    public PhoneBookRecord(long contactId, String name) {
        this.contactId = contactId;
        this.name = name;
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

    @Override
    public boolean equals(Object o) {
        return o != null && (o instanceof PhoneBookRecord) && equals((PhoneBookRecord) o);
    }

    public boolean equals(PhoneBookRecord o) {
        if (o.contactId != contactId) {
            return false;
        }
        if (!o.name.equals(name)) {
            return false;
        }

        for (PhoneBookPhone srcPhone : phones) {
            boolean founded = false;
            for (PhoneBookPhone destPhone : o.getPhones()) {
                if (srcPhone.equals(destPhone)) {
                    founded = true;
                    break;
                }
            }
            if (!founded) {
                return false;
            }
        }

        for (PhoneBookPhone srcPhone : o.getPhones()) {
            boolean founded = false;
            for (PhoneBookPhone destPhone : phones) {
                if (srcPhone.equals(destPhone)) {
                    founded = true;
                    break;
                }
            }
            if (!founded) {
                return false;
            }
        }


        for (PhoneBookEmail srcEmail : emails) {
            boolean founded = false;
            for (PhoneBookEmail destEmail : o.getEmails()) {
                if (srcEmail.equals(destEmail)) {
                    founded = true;
                    break;
                }
            }
            if (!founded) {
                return false;
            }
        }

        for (PhoneBookEmail srcEmail : o.getEmails()) {
            boolean founded = false;
            for (PhoneBookEmail destEmail : emails) {
                if (srcEmail.equals(destEmail)) {
                    founded = true;
                    break;
                }
            }
            if (!founded) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (contactId ^ (contactId >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (phones != null ? phones.hashCode() : 0);
        return result;
    }
}
