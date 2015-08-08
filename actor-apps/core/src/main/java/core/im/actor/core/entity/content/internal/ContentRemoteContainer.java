/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content.internal;

import im.actor.core.api.Message;

public class ContentRemoteContainer extends AbsContentContainer {
    private Message message;

    public ContentRemoteContainer(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
