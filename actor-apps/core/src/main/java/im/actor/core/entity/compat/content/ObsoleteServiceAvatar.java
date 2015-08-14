/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.compat.content;

import java.io.IOException;

import im.actor.core.api.ApiMessage;
import im.actor.core.api.ApiServiceExChangedAvatar;
import im.actor.core.api.ApiServiceMessage;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.core.entity.compat.ObsoleteAvatar;

public class ObsoleteServiceAvatar extends BserObject {

    private ObsoleteAvatar avatar;

    public ObsoleteServiceAvatar(BserValues values) throws IOException {
        parse(values);
    }

    public ApiMessage toApiMessage() {
        return new ApiServiceMessage("Avatar Changed", new ApiServiceExChangedAvatar(
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
