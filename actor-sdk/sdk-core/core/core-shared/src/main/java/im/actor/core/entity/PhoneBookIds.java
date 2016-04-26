/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class PhoneBookIds extends BserObject {

    public PhoneBookIds() {
    }

    public PhoneBookIds(List<Long> ids) {
        this.ids = ids;
    }

    List<Long> ids = new ArrayList<Long>();

    @Override
    public void parse(BserValues values) throws IOException {
        ids = values.getRepeatedLong(1);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeRepeatedLong(1, ids);
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}
