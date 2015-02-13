package im.actor.messenger.core.actors.contacts.book;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.SystemClock;
import android.provider.ContactsContract;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import im.actor.messenger.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by ex3ndr on 11.09.14.
 */
public class PhoneBookLoader {

    private static final boolean DISABLE_PHONE_BOOK = false;
    private static final String TAG = "PhoneBookLoader";
    private static PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();


    public static ArrayList<PhoneBookRecord> loadPhoneBook(Context context, String isoCountry) {
        if (DISABLE_PHONE_BOOK) {
            return new ArrayList<>();
        }

        Logger.d(TAG, "Loading phone book");
        long start = SystemClock.uptimeMillis();

        HashSet<Long> addedPhones = new HashSet<Long>();
        HashSet<String> addedEmails = new HashSet<String>();
        ArrayList<PhoneBookRecord> records = new ArrayList<PhoneBookRecord>();
        HashMap<Long, PhoneBookRecord> recordsMap = new HashMap<Long, PhoneBookRecord>();

        ContentResolver cr = context.getContentResolver();
        if (cr == null) {
            return new ArrayList<PhoneBookRecord>();
        }

        // Loading records
        // TODO: Better logic for duplicate phones
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                new String[]
                        {
                                ContactsContract.Contacts._ID,
                                ContactsContract.Contacts.DISPLAY_NAME
                        }, null, null, ContactsContract.Contacts.SORT_KEY_PRIMARY
        );
        if (cur == null) {
            return new ArrayList<PhoneBookRecord>();
        }
        int idIndex = cur.getColumnIndex(ContactsContract.Contacts._ID);
        int nameIndex = cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        while (cur.moveToNext()) {
            long id = cur.getLong(idIndex);
            String name = cur.getString(nameIndex);
            if (name == null || name.trim().length() == 0)
                continue;

            PhoneBookRecord record = new PhoneBookRecord(id, name.trim());
            records.add(record);
            recordsMap.put(id, record);
        }
        cur.close();
        cur = null;

        // Loading phones
        cur = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone._ID,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                null,
                null, ContactsContract.CommonDataKinds.Phone._ID + " desc"
        );
        if (cur == null) {
            return new ArrayList<PhoneBookRecord>();
        }

        final int idContactIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
        final int idPhoneIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID);
        final int idNumberIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        while (cur.moveToNext()) {
            long contactId = cur.getLong(idContactIndex);
            long phoneId = cur.getLong(idPhoneIndex);
            String rawPhone = cur.getString(idNumberIndex);
            PhoneBookRecord record = recordsMap.get(contactId);
            if (record == null) {
                continue;
            }

            try {
                final Phonenumber.PhoneNumber phonenumber = phoneUtil.parse(rawPhone, isoCountry);
                rawPhone = phonenumber.getCountryCode() + "" + phonenumber.getNationalNumber();
            } catch (final NumberParseException e) {
                rawPhone = rawPhone.replaceAll("[^\\d]", "");
            }
            long phone = -1;
            try {
                phone = Long.parseLong(rawPhone);
            } catch (Exception e) {
                // Logger.d(TAG, "Can't parse number", e);
                continue;
            }


            if (addedPhones.contains(phone)) {
                continue;
            }
            addedPhones.add(phone);

            record.getPhones().add(new PhoneBookPhone(phoneId, phone));
        }
        cur.close();
        cur = null;

        // Loading emails
        cur = cr.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Email._ID,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Email.ADDRESS
                },
                null,
                null, ContactsContract.CommonDataKinds.Email._ID + " desc"
        );

        if (cur == null) {
            return new ArrayList<PhoneBookRecord>();
        }

        final int idEmailContactIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID);
        final int idEmailIdIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Email._ID);
        final int idEmailIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);

        while (cur.moveToNext()) {
            long contactId = cur.getLong(idEmailContactIndex);
            long emailId = cur.getLong(idEmailIdIndex);
            String rawEmail = cur.getString(idEmailIndex);
            PhoneBookRecord record = recordsMap.get(contactId);
            if (record == null) {
                continue;
            }

            if (rawEmail == null) {
                continue;
            }
            rawEmail = rawEmail.toLowerCase();

            if (addedEmails.contains(rawEmail)) {
                continue;
            }
            addedEmails.add(rawEmail);

            record.getEmails().add(new PhoneBookEmail(emailId, rawEmail));
        }
        cur.close();

        // Filtering records without contacts
        ArrayList<PhoneBookRecord> res = new ArrayList<PhoneBookRecord>();
        for (PhoneBookRecord rec : records) {
            if (rec.getPhones().size() > 0 || rec.getEmails().size() > 0) {
                res.add(rec);
            }
        }
        Logger.d(TAG, "Phone book loaded in " + (SystemClock.uptimeMillis() - start) + " ms");
        return res;
    }
}
