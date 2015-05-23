/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.compat.content;

import java.io.IOException;

import im.actor.model.api.Message;
import im.actor.model.api.ServiceExContactRegistered;
import im.actor.model.api.ServiceMessage;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

/**
 * Created by ex3ndr on 24.05.15.
 */
public class ObsoleteServiceRegistered extends ObsoleteAbsContent {
    @Override
    public Message toApiMessage() {
        return new ServiceMessage("User registered", new ServiceExContactRegistered(/*???*/));
    }

    @Override
    public void parse(BserValues values) throws IOException {

    }

    @Override
    public void serialize(BserWriter writer) throws IOException {

    }
}
