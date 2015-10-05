/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

public class ContactRecord {

    @Property("readonly, nonatomic")
    private final ContactRecordType recordType;
    @Property("readonly, nonatomic")
    private final String recordData;
    @Property("readonly, nonatomic")
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
