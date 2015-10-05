/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content.internal;

import im.actor.core.api.ApiMessage;

public class ContentRemoteContainer extends AbsContentContainer {
    private ApiMessage message;

    public ContentRemoteContainer(ApiMessage message) {
        this.message = message;
    }

    public ApiMessage getMessage() {
        return message;
    }
}
