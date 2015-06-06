/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.compat.content;

import java.io.IOException;

import im.actor.model.api.Message;
import im.actor.model.api.ServiceMessage;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

public class ObsoleteService extends BserObject {
    private String compatText;

    public ObsoleteService(BserValues values) throws IOException {
        parse(values);
    }

    public Message toApiMessage() {
        return new ServiceMessage(compatText, null);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        compatText = values.getString(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        throw new UnsupportedOperationException();
    }
}
