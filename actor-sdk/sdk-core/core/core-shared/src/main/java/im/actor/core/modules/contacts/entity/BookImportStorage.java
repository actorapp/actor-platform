package im.actor.core.modules.contacts.entity;

import java.io.IOException;
import java.util.HashSet;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class BookImportStorage extends BserObject {

    private HashSet<String> importedEmails = new HashSet<>();
    private HashSet<Long> importedPhones = new HashSet<>();

    public BookImportStorage() {
    }

    public BookImportStorage(byte[] data) {
        super();
        try {
            super.load(data);
        } catch (IOException e) {
            e.printStackTrace();
            importedEmails = new HashSet<>();
            importedPhones = new HashSet<>();
        }
    }

    public void markAsImported(String email) {
        importedEmails.add(email);
    }

    public void markAsImported(long phone) {
        importedPhones.add((Long) phone);
    }

    public boolean isImported(String email) {
        return importedEmails.contains(email);
    }

    public boolean isImported(long phone) {
        return importedPhones.contains(phone);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        importedEmails = new HashSet<>();
        importedPhones = new HashSet<>();

        for (String s : values.getRepeatedString(1)) {
            importedEmails.add(s);
        }

        for (Long p : values.getRepeatedLong(2)) {
            importedPhones.add(p);
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        for (String s : importedEmails) {
            writer.writeString(1, s);
        }
        for (Long p : importedPhones) {
            writer.writeLong(2, p);
        }
    }
}
