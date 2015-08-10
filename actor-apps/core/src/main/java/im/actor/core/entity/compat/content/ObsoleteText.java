/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.compat.content;

import java.io.IOException;
import java.util.ArrayList;

import im.actor.core.api.Message;
import im.actor.core.api.TextMessage;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class ObsoleteText extends BserObject {

    private String text;

    public ObsoleteText(BserValues values) throws IOException {
        parse(values);
    }

    public Message toApiMessage() {
        return new TextMessage(text, new ArrayList<Integer>(), null);
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
