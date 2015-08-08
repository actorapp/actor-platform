/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import im.actor.core.api.ServiceExUserJoined;
import im.actor.core.api.ServiceMessage;
import im.actor.core.entity.content.internal.ContentRemoteContainer;

public class ServiceGroupUserJoined extends ServiceContent {

    public static ServiceGroupUserJoined create() {
        return new ServiceGroupUserJoined(new ContentRemoteContainer(new ServiceMessage("User joined",
                new ServiceExUserJoined())));
    }

    public ServiceGroupUserJoined(ContentRemoteContainer contentContainer) {
        super(contentContainer);
    }
}
