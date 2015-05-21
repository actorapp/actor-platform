/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity;

public class ContactRecord {

    private final ContactRecordType recordType;
    private final String recordData;
    private final String recordTitle;

    public ContactRecord(ContactRecordType recordType, String recordData, String recordTitle) {
        this.recordType = recordType;
        this.recordData = recordData;
        this.recordTitle = recordTitle;
    }

    public ContactRecordType getRecordType() {
        return recordType;
    }

    public String getRecordData() {
        return recordData;
    }

    public String getRecordTitle() {
        return recordTitle;
    }
}
