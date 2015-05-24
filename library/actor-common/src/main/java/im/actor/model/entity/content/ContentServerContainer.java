/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content;

import im.actor.model.api.Message;

public class ContentServerContainer extends AbsContentContainer {
    private Message message;

    public ContentServerContainer(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
