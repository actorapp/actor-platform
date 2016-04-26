/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import java.io.IOException;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class PhoneBookEmail extends BserObject {
    @Property("readonly, nonatomic")
    private long id;
    @Property("readonly, nonatomic")
    private String email;

    public PhoneBookEmail(long id, String email) {
        this.id = id;
        this.email = email;
    }

    public PhoneBookEmail() {

    }

    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }


    @Override
    public void parse(BserValues values) throws IOException {
        id = values.getLong(1);
        email = values.getString(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, id);
        writer.writeString(2, email);
    }
}
