/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.compat.content;

import java.io.IOException;

import im.actor.core.api.ApiMessage;
import im.actor.core.api.ApiServiceExUserInvited;
import im.actor.core.api.ApiServiceMessage;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class ObsoleteServiceAdded extends BserObject {

    private int addedUid;

    public ObsoleteServiceAdded(BserValues values) throws IOException {
        parse(values);
    }

    public ApiMessage toApiMessage() {
        return new ApiServiceMessage("Member added",
                new ApiServiceExUserInvited(addedUid));
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
