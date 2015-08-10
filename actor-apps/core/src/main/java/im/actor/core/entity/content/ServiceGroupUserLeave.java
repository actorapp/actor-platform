/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import im.actor.core.api.ServiceExUserLeft;
import im.actor.core.api.ServiceMessage;
import im.actor.core.entity.content.internal.ContentRemoteContainer;

public class ServiceGroupUserLeave extends ServiceContent {

    public static ServiceGroupUserLeave create() {
        return new ServiceGroupUserLeave(new ContentRemoteContainer(new ServiceMessage("User leave",
                new ServiceExUserLeft())));
    }

    public ServiceGroupUserLeave(ContentRemoteContainer contentContainer) {
        super(contentContainer);
    }
}
