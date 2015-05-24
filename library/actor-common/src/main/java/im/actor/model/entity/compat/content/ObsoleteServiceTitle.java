/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.compat.content;

import java.io.IOException;

import im.actor.model.api.Message;
import im.actor.model.api.ServiceExChangedTitle;
import im.actor.model.api.ServiceMessage;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

/**
 * Created by ex3ndr on 24.05.15.
 */
public class ObsoleteServiceTitle extends ObsoleteAbsContent {
    private String newTitle;

    public ObsoleteServiceTitle(byte[] data) throws IOException {
        load(data);
    }

    @Override
    public Message toApiMessage() {
        return new ServiceMessage("Group theme changed",
                new ServiceExChangedTitle(newTitle));
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
