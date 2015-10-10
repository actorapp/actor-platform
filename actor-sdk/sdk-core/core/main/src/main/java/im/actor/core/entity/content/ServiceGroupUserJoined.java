/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import im.actor.core.api.ApiServiceExUserJoined;
import im.actor.core.api.ApiServiceMessage;
import im.actor.core.entity.content.internal.ContentRemoteContainer;

public class ServiceGroupUserJoined extends ServiceContent {

    public static ServiceGroupUserJoined create() {
        return new ServiceGroupUserJoined(new ContentRemoteContainer(new ApiServiceMessage("User joined",
                new ApiServiceExUserJoined())));
    }

    public ServiceGroupUserJoined(ContentRemoteContainer contentContainer) {
        super(contentContainer);
    }
}
