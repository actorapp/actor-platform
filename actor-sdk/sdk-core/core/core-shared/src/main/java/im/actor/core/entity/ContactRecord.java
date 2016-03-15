/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;


public class ContactRecord {

    @Property("readonly, nonatomic")
    private final ContactRecordType recordType;
    @Property("readonly, nonatomic")
    private final String recordTypeSpec;
    @Property("readonly, nonatomic")
    private final String recordData;
    @Property("readonly, nonatomic")
    private final String recordTitle;
    @Property("readonly, nonatomic")
    private final String recordSubtitle;

    public ContactRecord(ContactRecordType recordType, String recordTypeSpec, String recordData, String recordTitle, String recordSubtitle) {
        this.recordType = recordType;
        this.recordTypeSpec = recordTypeSpec;
        this.recordData = recordData;
        this.recordTitle = recordTitle;
        this.recordSubtitle = recordSubtitle;
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

    public String getRecordTypeSpec() {
        return recordTypeSpec;
    }

    public String getRecordSubtitle() {
        return recordSubtitle;
    }
}
