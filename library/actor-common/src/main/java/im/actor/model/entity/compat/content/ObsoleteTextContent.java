/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.compat.content;

import java.io.IOException;
import java.util.ArrayList;

import im.actor.model.api.Message;
import im.actor.model.api.TextMessage;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

public class ObsoleteTextContent extends ObsoleteAbsContent {

    private String text;

    public ObsoleteTextContent(byte[] data) throws IOException {
        load(data);
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

    @Override
    public Message toApiMessage() {
        return new TextMessage(text, new ArrayList<Integer>(), null);
    }
}
