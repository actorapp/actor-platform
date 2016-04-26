/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import java.io.IOException;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class PhoneBookPhone extends BserObject {
    @Property("readonly, nonatomic")
    private long id;
    @Property("readonly, nonatomic")
    private long number;

    public PhoneBookPhone(long id, long number) {
        this.id = id;
        this.number = number;
    }

    public PhoneBookPhone() {

    }

    public long getId() {
        return id;
    }

    public long getNumber() {
        return number;
    }


    @Override
    public void parse(BserValues values) throws IOException {
        id = values.getLong(1);
        number = values.getLong(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, id);
        writer.writeLong(2, number);
    }
}
