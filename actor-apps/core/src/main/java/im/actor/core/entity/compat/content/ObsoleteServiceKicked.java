/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.compat.content;

import java.io.IOException;

import im.actor.core.api.ApiMessage;
import im.actor.core.api.ApiServiceExUserKicked;
import im.actor.core.api.ApiServiceMessage;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class ObsoleteServiceKicked extends BserObject {

    private int kickedUid;

    public ObsoleteServiceKicked(BserValues values) throws IOException {
        parse(values);
    }

    public ApiMessage toApiMessage() {
        return new ApiServiceMessage("Member kicked",
                new ApiServiceExUserKicked(kickedUid));
    }

    @Override
    public void parse(BserValues values) throws IOException {
        kickedUid = values.getInt(10);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        throw new UnsupportedOperationException();
    }
}
