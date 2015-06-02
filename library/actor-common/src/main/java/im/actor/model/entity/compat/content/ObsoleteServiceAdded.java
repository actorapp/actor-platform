/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.compat.content;

import java.io.IOException;

import im.actor.model.api.Message;

import im.actor.model.api.ServiceExUserInvited;
import im.actor.model.api.ServiceMessage;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

public class ObsoleteServiceAdded extends BserObject {

    private int addedUid;

    public ObsoleteServiceAdded(BserValues values) throws IOException {
        parse(values);
    }

    public Message toApiMessage() {
        return new ServiceMessage("Member added",
                new ServiceExUserInvited(addedUid));
    }

    @Override
    public void parse(BserValues values) throws IOException {
        addedUid = values.getInt(10);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        throw new UnsupportedOperationException();
    }
}
