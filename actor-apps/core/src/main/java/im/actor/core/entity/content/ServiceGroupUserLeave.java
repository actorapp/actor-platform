/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import im.actor.core.api.ApiServiceExUserLeft;
import im.actor.core.api.ApiServiceMessage;
import im.actor.core.entity.content.internal.ContentRemoteContainer;

public class ServiceGroupUserLeave extends ServiceContent {

    public static ServiceGroupUserLeave create() {
        return new ServiceGroupUserLeave(new ContentRemoteContainer(new ApiServiceMessage("User leave",
                new ApiServiceExUserLeft())));
    }

    public ServiceGroupUserLeave(ContentRemoteContainer contentContainer) {
        super(contentContainer);
    }
}
