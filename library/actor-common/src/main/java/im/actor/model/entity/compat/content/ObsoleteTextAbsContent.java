/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.compat.content;

import java.io.IOException;

import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

/**
 * Created by ex3ndr on 24.05.15.
 */
public class ObsoleteTextAbsContent extends ObsoleteAbsContent {

    private String text;

    public ObsoleteTextAbsContent(BserValues values) throws IOException {
        parse(values);
    }

    public String getText() {
        return text;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        text = values.getString(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        throw new UnsupportedOperationException();
    }
}
