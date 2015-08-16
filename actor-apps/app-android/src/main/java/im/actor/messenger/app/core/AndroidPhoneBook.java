package im.actor.messenger.app.core;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.SystemClock;
import android.provider.ContactsContract;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import im.actor.messenger.app.AppContext;
import im.actor.messenger.app.util.Logger;
import im.actor.messenger.app.util.country.CountryUtil;
import im.actor.core.PhoneBookProvider;
import im.actor.core.entity.PhoneBookContact;
import im.actor.core.entity.PhoneBookEmail;
import im.actor.core.entity.PhoneBookPhone;

/**
 * Created by ex3ndr on 25.02.15.
 */
public class AndroidPhoneBook implements PhoneBookProvider {

    private static final int PRELOAD_DELAY = 3000;
    private static final int READ_ITEM_DELAY_BATCH = 30;
    private static final int READ_ITEM_DELAY = 10;
    private static final boolean DISABLE_PHONE_BOOK = false;
    private static final String TAG = "PhoneBookLoader";
    private static PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    @Override
    public void loadPhoneBook(final Callback callback) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(PRELOAD_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ArrayList<PhoneBookContact> contacts = loadPhoneBook(AppContext.getContext(),
                        CountryUtil.getDeviceCountry(AppContext.getContext()));
                callback.onLoaded(contacts);
            }
        }.start();
    }

    public static ArrayList<PhoneBookContact> loadPhoneBook(Context context, String isoCountry) {
        if (DISABLE_PHONE_BOOK) {
            return new ArrayList<PhoneBookContact>();
        }

        Logger.d(TAG, "Loading phone book");
        long start = SystemClock.uptimeMillis();

        HashSet<Long> addedPhones = new HashSet<Long>();
        HashSet<String> addedEmails = new HashSet<String>();
        ArrayList<PhoneBookContact> records = new ArrayList<PhoneBookContact>();
        HashMap<Long, PhoneBookContact> recordsMap = new HashMap<Long, PhoneBookContact>();

        ContentResolver cr = context.getContentResolver();
        if (cr == null) {
            return new ArrayList<PhoneBookContact>();
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
            return new ArrayList<PhoneBookContact>();
        }
        int idIndex = cur.getColumnIndex(ContactsContract.Contacts._ID);
        int nameIndex = cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

        int index = 0;
        while (cur.moveToNext()) {
            if (index++ == READ_ITEM_DELAY_BATCH) {
                if (READ_ITEM_DELAY > 0) {
                    try {
                        Thread.sleep(READ_ITEM_DELAY);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            long id = cur.getLong(idIndex);
            String name = cur.getString(nameIndex);
            if (name == null || name.trim().length() == 0)
                continue;

            PhoneBookContact record = new PhoneBookContact(id, name.trim(),
                    new ArrayList<PhoneBookPhone>(),
                    new ArrayList<PhoneBookEmail>());
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
            return new ArrayList<PhoneBookContact>();
        }

        final int idContactIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
        final int idPhoneIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID);
        final int idNumberIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);


        while (cur.moveToNext()) {
            if (index++ == READ_ITEM_DELAY_BATCH) {
                if (READ_ITEM_DELAY > 0) {
                    try {
                        Thread.sleep(READ_ITEM_DELAY);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            long contactId = cur.getLong(idContactIndex);
            long phoneId = cur.getLong(idPhoneIndex);
            String rawPhone = cur.getString(idNumberIndex);
            PhoneBookContact record = recordsMap.get(contactId);
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
            return new ArrayList<PhoneBookContact>();
        }

        final int idEmailContactIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID);
        final int idEmailIdIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Email._ID);
        final int idEmailIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);

        while (cur.moveToNext()) {
            if (index++ == READ_ITEM_DELAY_BATCH) {
                if (READ_ITEM_DELAY > 0) {
                    try {
                        Thread.sleep(READ_ITEM_DELAY);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            long contactId = cur.getLong(idEmailContactIndex);
            long emailId = cur.getLong(idEmailIdIndex);
            String rawEmail = cur.getString(idEmailIndex);
            PhoneBookContact record = recordsMap.get(contactId);
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
        ArrayList<PhoneBookContact> res = new ArrayList<PhoneBookContact>();
        for (PhoneBookContact rec : records) {
            if (rec.getPhones().size() > 0 || rec.getEmails().size() > 0) {
                res.add(rec);
            }
        }
        Logger.d(TAG, "Phone book loaded in " + (SystemClock.uptimeMillis() - start) + " ms in " + (index) + " iterations");
        return res;
    }
}
