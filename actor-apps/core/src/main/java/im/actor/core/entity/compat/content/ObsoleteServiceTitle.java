/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.compat.content;

import java.io.IOException;

import im.actor.core.api.ApiMessage;
import im.actor.core.api.ApiServiceExChangedTitle;
import im.actor.core.api.ApiServiceMessage;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class ObsoleteServiceTitle extends BserObject {
    private String newTitle;

    public ObsoleteServiceTitle(BserValues values) throws IOException {
        parse(values);
    }

    public ApiMessage toApiMessage() {
        return new ApiServiceMessage("Group theme changed",
                new ApiServiceExChangedTitle(newTitle));
    }

    @Override
    public void parse(BserValues values) throws IOException {
        newTitle = values.getString(10);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        throw new UnsupportedOperationException();
    }
}
