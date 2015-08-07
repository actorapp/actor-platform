/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content;

import im.actor.model.api.ServiceExUserJoined;
import im.actor.model.api.ServiceMessage;
import im.actor.model.entity.content.internal.ContentRemoteContainer;

public class ServiceGroupUserJoined extends ServiceContent {

    public static ServiceGroupUserJoined create() {
        return new ServiceGroupUserJoined(new ContentRemoteContainer(new ServiceMessage("User joined",
                new ServiceExUserJoined())));
    }

    public ServiceGroupUserJoined(ContentRemoteContainer contentContainer) {
        super(contentContainer);
    }
}
