/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.compat.content;

import java.io.IOException;

import im.actor.model.api.Message;
import im.actor.model.api.ServiceExChangedAvatar;
import im.actor.model.api.ServiceMessage;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.entity.compat.ObsoleteAvatar;

public class ObsoleteServiceAvatar extends BserObject {

    private ObsoleteAvatar avatar;

    public ObsoleteServiceAvatar(BserValues values) throws IOException {
        parse(values);
    }

    public Message toApiMessage() {
        return new ServiceMessage("Avatar Changed", new ServiceExChangedAvatar(
                avatar != null ? avatar.toApiAvatar() : null
        ));
    }

    @Override
    public void parse(BserValues values) throws IOException {
        //TODO: Should we use new avatar instead of old?
        byte[] data = values.optBytes(10);
        if (data != null) {
            avatar = new ObsoleteAvatar(data);
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        throw new UnsupportedOperationException();
    }
}
